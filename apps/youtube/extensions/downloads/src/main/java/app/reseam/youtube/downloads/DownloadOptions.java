// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.downloads;

import app.reseam.youtube.core.Logger;
import app.reseam.youtube.core.Proto;

public final class DownloadOptions {
    private static final int PICKER_BODY = 372095937;
    private static final long PREMIUM_LOCKED = 2;
    private static final long PREMIUM_UNLOCKED = 3;
    private static final long PREMIUM_UPSELL_SECTION = 2;

    private DownloadOptions() {}

    public static byte[] unlock(byte[] response) {
        try {
            byte[] picker = QualityPicker.replaceUpsell(response);
            if (picker != null) return picker;
            byte[] unlocked = rewrite(response);
            return unlocked == response ? null : unlocked;
        } catch (IllegalArgumentException exception) {
            Logger.error(() -> "Download action response is not a protobuf message", exception);
            return null;
        }
    }

    private static byte[] rewrite(byte[] message) {
        return Proto.rewriteLength(message, (number, value) -> number == PICKER_BODY ? body(value) : nested(value));
    }

    private static byte[] nested(byte[] value) {
        try {
            return rewrite(value);
        } catch (IllegalArgumentException notMessage) {
            return value;
        }
    }

    private static byte[] body(byte[] model) {
        return Proto.rewriteLength(model, (number, value) -> switch (number) {
            case 1 -> options(withoutUpsell(value));
            case 2 -> options(value);
            default -> value;
        });
    }

    private static byte[] options(byte[] message) {
        return Proto.rewriteLength(message, (number, option) -> number != 2 ? option
                : Proto.rewriteNumber(option, 4, availability ->
                        availability == PREMIUM_LOCKED ? PREMIUM_UNLOCKED : availability));
    }

    private static byte[] withoutUpsell(byte[] optionList) {
        return Proto.rewriteLength(optionList, (number, section) ->
                number == 12 && Proto.number(section, 1, 0) == PREMIUM_UPSELL_SECTION ? null : section);
    }
}
