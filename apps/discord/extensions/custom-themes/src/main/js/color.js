// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

const PALETTE_TONES = [0, 4, 6, 10, 12, 17, 20, 22, 24, 30, 40, 50, 60, 70, 80, 87, 90, 92, 94, 95, 96, 98, 99, 100];

function parseColor(text) {
    const match = /^#([0-9a-f]{6})([0-9a-f]{2})?$/i.exec(text);
    if (match === null) throw new Error(`${text} is not a #RRGGBB or #RRGGBBAA color`);
    const value = parseInt(match[1], 16);
    const alpha = match[2] === undefined ? 255 : parseInt(match[2], 16);
    return { r: (value >> 16 & 255) / 255, g: (value >> 8 & 255) / 255, b: (value & 255) / 255, a: alpha / 255 };
}

function hexByte(channel) {
    return Math.round(Math.min(1, Math.max(0, channel)) * 255).toString(16).padStart(2, "0");
}

function opaqueHex(color) {
    return `#${hexByte(color.r)}${hexByte(color.g)}${hexByte(color.b)}`.toUpperCase();
}

function formatColor(color) {
    const alpha = Math.round(color.a * 255);
    return alpha === 255 ? opaqueHex(color) : `${opaqueHex(color)}${hexByte(color.a).toUpperCase()}`;
}

function withAlpha(color, a) {
    return { r: color.r, g: color.g, b: color.b, a };
}

function argb(color) {
    const byte = channel => Math.round(Math.min(1, Math.max(0, channel)) * 255);
    return ((byte(color.a) << 24) | (byte(color.r) << 16) | (byte(color.g) << 8) | byte(color.b)) >>> 0;
}

function fromRgbInt(value, a) {
    return { r: (value >> 16 & 255) / 255, g: (value >> 8 & 255) / 255, b: (value & 255) / 255, a };
}

function rgbInt(color) {
    return argb(color) & 0xffffff;
}

function linear(channel) {
    return channel <= 0.04045 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4;
}

function gamma(channel) {
    return channel <= 0.0031308 ? channel * 12.92 : 1.055 * channel ** (1 / 2.4) - 0.055;
}

function toneOf(color) {
    const y = 0.2126 * linear(color.r) + 0.7152 * linear(color.g) + 0.0722 * linear(color.b);
    return y > 216 / 24389 ? 116 * Math.cbrt(y) - 16 : y * 24389 / 27;
}

function toOklab(color) {
    const r = linear(color.r);
    const g = linear(color.g);
    const b = linear(color.b);
    const l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
    const m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
    const s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
    return {
        L: 0.2104542553 * l + 0.793617785 * m - 0.0040720468 * s,
        A: 1.9779984951 * l - 2.428592205 * m + 0.4505937099 * s,
        B: 0.0259040371 * l + 0.7827717662 * m - 0.808675766 * s,
    };
}

function oklabToLinear(lab) {
    const l = (lab.L + 0.3963377774 * lab.A + 0.2158037573 * lab.B) ** 3;
    const m = (lab.L - 0.1055613458 * lab.A - 0.0638541728 * lab.B) ** 3;
    const s = (lab.L - 0.0894841775 * lab.A - 1.291485548 * lab.B) ** 3;
    return [
        4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
        -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
        -0.0041960863 * l - 0.7034186147 * m + 1.707614701 * s,
    ];
}

function fromOklab(lab, a) {
    const [r, g, b] = oklabToLinear(lab).map(channel => gamma(Math.min(1, Math.max(0, channel))));
    return { r, g, b, a };
}

function inGamut(lab) {
    return oklabToLinear(lab).every(channel => channel >= -1e-4 && channel <= 1 + 1e-4);
}

function mixOklab(from, to, amount) {
    const a = toOklab(from);
    const b = toOklab(to);
    return fromOklab({ L: a.L + (b.L - a.L) * amount, A: a.A + (b.A - a.A) * amount, B: a.B + (b.B - a.B) * amount }, 1);
}

function paletteAt(palette, tone, a) {
    const upper = palette.findIndex(([stop]) => stop >= tone);
    if (upper === 0) return withAlpha(palette[0][1], a);
    if (upper === -1) return withAlpha(palette[palette.length - 1][1], a);
    const [lowTone, low] = palette[upper - 1];
    const [highTone, high] = palette[upper];
    return withAlpha(mixOklab(low, high, (tone - lowTone) / (highTone - lowTone)), a);
}

function gamutColor(L, hue, chroma) {
    const at = c => ({ L, A: c * Math.cos(hue), B: c * Math.sin(hue) });
    if (inGamut(at(chroma))) return at(chroma);
    let low = 0;
    let high = chroma;
    for (let step = 0; step < 16; step++) {
        const middle = (low + high) / 2;
        if (inGamut(at(middle))) low = middle; else high = middle;
    }
    return at(low);
}

function paletteFromSeed(seed) {
    const lab = toOklab(seed);
    const hue = Math.atan2(lab.B, lab.A);
    const chroma = Math.hypot(lab.A, lab.B);
    return PALETTE_TONES.map(tone => {
        let low = 0;
        let high = 1;
        for (let step = 0; step < 24; step++) {
            const middle = (low + high) / 2;
            if (toneOf(fromOklab(gamutColor(middle, hue, chroma), 1)) < tone) low = middle; else high = middle;
        }
        return [tone, fromOklab(gamutColor((low + high) / 2, hue, chroma), 1)];
    });
}
