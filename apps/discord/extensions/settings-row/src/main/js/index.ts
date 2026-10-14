// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

import { NativeModules } from "react-native";

const RESEAM_SETTINGS = "RESEAM_SETTINGS";

// Discord reads rows from this frozen map on every lookup, so a copy with the Reseam row replaces it.
export function addReseamRow(settings: { SETTING_RENDERER_CONFIG: Record<string, any> }) {
    const rows = settings.SETTING_RENDERER_CONFIG;
    settings.SETTING_RENDERER_CONFIG = Object.freeze({
        ...rows,
        [RESEAM_SETTINGS]: {
            type: "pressable",
            parent: null,
            useTitle: () => "Reseam Settings",
            IconComponent: rows.ADVANCED.IconComponent,
            onPress: () => NativeModules.ReseamSettings.openSettings("reseam-patches"),
            useSearchTerms: () => ["Reseam", "Patches"],
            withArrow: true,
        },
    });
}

export function withReseamSection<List extends { sections: { settings: string[] }[] }>(list: List): List {
    const sections = [...list.sections];
    const appSettings = sections.findIndex(section => section.settings.includes("ADVANCED"));
    if (appSettings === -1) throw new Error("Discord settings have no App Settings section");
    sections.splice(appSettings + 1, 0, { label: "Reseam", settings: [RESEAM_SETTINGS] } as List["sections"][number]);
    return { ...list, sections };
}
