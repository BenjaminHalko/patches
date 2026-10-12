// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.discord.themes;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public final class ThemesModule extends ReactContextBaseJavaModule {
    static final String NAME = "ReseamDiscordThemes";
    private static final String MIME_TYPE = "application/json";

    ThemesModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String readThemes() throws IOException {
        return ThemeFiles.read(ThemeFiles.themes(getReactApplicationContext()));
    }

    @ReactMethod
    public void writeThemes(String json) throws IOException {
        ThemeFiles.write(ThemeFiles.themes(getReactApplicationContext()), json);
    }

    @ReactMethod
    public void applyNativeColors(String json) throws IOException, JSONException {
        ThemeColors.apply(getReactApplicationContext(), json);
    }

    @ReactMethod
    public void share(String fileName, String json, String title) throws IOException {
        Activity activity = getCurrentActivity();
        if (activity == null) return;
        File directory = ThemeFiles.shared(activity);
        File[] previous = directory.listFiles();
        if (previous != null) for (File file : previous) file.delete();
        File file = new File(directory, fileName);
        ThemeFiles.write(file, json);
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".file-provider", file);
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType(MIME_TYPE)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        send.setClipData(ClipData.newRawUri(fileName, uri));
        activity.runOnUiThread(() -> activity.startActivity(Intent.createChooser(send, title)));
    }
}
