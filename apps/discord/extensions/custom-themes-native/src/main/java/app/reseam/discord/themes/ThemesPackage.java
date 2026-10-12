// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.discord.themes;

import com.facebook.react.BaseReactPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;

import java.util.Collections;
import java.util.List;

public final class ThemesPackage extends BaseReactPackage {
    public static void addTo(List<ReactPackage> packages) {
        packages.add(new ThemesPackage());
    }

    @Override
    public NativeModule getModule(String name, ReactApplicationContext context) {
        return ThemesModule.NAME.equals(name) ? new ThemesModule(context) : null;
    }

    @Override
    public ReactModuleInfoProvider getReactModuleInfoProvider() {
        ReactModuleInfo info = new ReactModuleInfo(ThemesModule.NAME,
                ThemesModule.class.getName(), false, false, false, false);
        return () -> Collections.singletonMap(ThemesModule.NAME, info);
    }
}
