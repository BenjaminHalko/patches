// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.discord.themes;

import android.content.Context;
import android.util.AtomicFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ThemeFiles {
    private ThemeFiles() {}

    static File directory(Context context) {
        return new File(context.getFilesDir(), "reseam-patches/discord-themes");
    }

    static File themes(Context context) {
        return new File(directory(context), "themes.json");
    }

    static File nativeColors(Context context) {
        return new File(directory(context), "native-colors.json");
    }

    static File shared(Context context) {
        return new File(directory(context), "shared");
    }

    static String read(File file) throws IOException {
        try {
            return new String(new AtomicFile(file).readFully(), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            return "";
        }
    }

    static void write(File file, String text) throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) throw new IOException("Cannot create " + parent);
        AtomicFile atomic = new AtomicFile(file);
        FileOutputStream out = atomic.startWrite();
        try {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            atomic.finishWrite(out);
        } catch (IOException e) {
            atomic.failWrite(out);
            throw e;
        }
    }
}
