/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.accompanist.testharness;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.LayoutInflater;

import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;

/**
 * A context wrapper that allows you to modify or replace the theme of the wrapped context.
 * <p>
 * This is a modification of androidx.appcompat.view.ContextThemeWrapper to allow the
 * TestHarness to work in previews.
 */
class ContextThemeWrapper extends ContextWrapper {
    /**
     * Lazily-populated configuration object representing an empty, default configuration.
     */
    private static Configuration sEmptyConfig;

    private int mThemeResource;
    private Resources.Theme mTheme;
    private LayoutInflater mInflater;
    private Configuration mOverrideConfiguration;
    private Resources mResources;

    /**
     * Creates a new context wrapper with no theme and no base context.
     * <p class="note">
     * <strong>Note:</strong> A base context <strong>must</strong> be attached
     * using {@link #attachBaseContext(Context)} before calling any other
     * method on the newly constructed context wrapper.
     */
    public ContextThemeWrapper() {
        super(null);
    }

    /**
     * Creates a new context wrapper with the specified theme.
     * <p>
     * The specified theme will be applied on top of the base context's theme.
     * Any attributes not explicitly defined in the theme identified by
     * <var>themeResId</var> will retain their original values.
     *
     * @param base the base context
     * @param themeResId the resource ID of the theme to be applied on top of
     *                   the base context's theme
     */
    public ContextThemeWrapper(Context base, @StyleRes int themeResId) {
        super(base);
        mThemeResource = themeResId;
    }

    /**
     * Creates a new context wrapper with the specified theme.
     * <p>
     * Unlike {@link #ContextThemeWrapper(Context, int)}, the theme passed to
     * this constructor will completely replace the base context's theme.
     *
     * @param base the base context
     * @param theme the theme against which resources should be inflated
     */
    public ContextThemeWrapper(Context base, Resources.Theme theme) {
        super(base);
        mTheme = theme;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    /**
     * Call to set an "override configuration" on this context -- this is
     * a configuration that replies one or more values of the standard
     * configuration that is applied to the context.  See
     * {@link Context#createConfigurationContext(Configuration)} for more
     * information.
     *
     * <p>This method can only be called once, and must be called before any
     * calls to {@link #getResources()} or {@link #getAssets()} are made.
     */
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (mResources != null) {
            throw new IllegalStateException(
                "getResources() or getAssets() has already been called");
        }
        if (mOverrideConfiguration != null) {
            throw new IllegalStateException("Override configuration has already been set");
        }
        mOverrideConfiguration = new Configuration(overrideConfiguration);
    }

    @Override
    public Resources getResources() {
        return getResourcesInternal();
    }

    private Resources getResourcesInternal() {
        if (mResources == null) {
            if (mOverrideConfiguration == null
                || (Build.VERSION.SDK_INT >= 26
                    && isEmptyConfiguration(mOverrideConfiguration))) {
                // If we're not applying any overrides, use the base context's resources. On API
                // 26+, this will avoid pulling in resources that share a backing implementation
                // with the application context.
                mResources = super.getResources();
            } else {
                final Context resContext = createConfigurationContext(mOverrideConfiguration);

                // MAIN UPDATE: If resContext is null, fallback to the deprecated Resources
                // constructor instead of crashing
                if (resContext != null) {
                    mResources = resContext.getResources();
                } else {
                    Resources res = super.getResources();
                    Configuration newConfig = new Configuration(res.getConfiguration());
                    newConfig.updateFrom(mOverrideConfiguration);
                    mResources = new Resources(res.getAssets(), res.getDisplayMetrics(), newConfig);
                }
            }
        }
        return mResources;
    }

    @Override
    public void setTheme(int resid) {
        if (mThemeResource != resid) {
            mThemeResource = resid;
            initializeTheme();
        }
    }

    /**
     * Returns the resource ID of the theme that is to be applied on top of the base context's
     * theme.
     */
    public int getThemeResId() {
        return mThemeResource;
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme != null) {
            return mTheme;
        }

        initializeTheme();

        return mTheme;
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return mInflater;
        }
        return getBaseContext().getSystemService(name);
    }

    /**
     * Called by {@link #setTheme} and {@link #getTheme} to apply a theme
     * resource to the current Theme object.  Can override to change the
     * default (simple) behavior.  This method will not be called in multiple
     * threads simultaneously.
     *
     * @param theme The Theme object being modified.
     * @param resid The theme style resource being applied to <var>theme</var>.
     * @param first Set to true if this is the first time a style is being
     *              applied to <var>theme</var>.
     */
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(resid, true);
    }

    private void initializeTheme() {
        final boolean first = mTheme == null;
        if (first) {
            mTheme = getResources().newTheme();
            Resources.Theme theme = getBaseContext().getTheme();
            if (theme != null) {
                mTheme.setTo(theme);
            }
        }
        onApplyThemeResource(mTheme, mThemeResource, first);
    }

    @Override
    public AssetManager getAssets() {
        // Ensure we're returning assets with the correct configuration.
        return getResources().getAssets();
    }

    /**
     * @return {@code true} if the specified configuration is {@code null} or is a no-op when
     *         used as a configuration overlay
     */
    @RequiresApi(26)
    private static boolean isEmptyConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration == null) {
            return true;
        }

        if (sEmptyConfig == null) {
            Configuration emptyConfig = new Configuration();
            // Workaround for incorrect default fontScale on earlier SDKs (b/29924927). Note
            // that Configuration.setToDefaults() is *not* a no-op configuration overlay.
            emptyConfig.fontScale = 0.0f;
            sEmptyConfig = emptyConfig;
        }

        return overrideConfiguration.equals(sEmptyConfig);
    }
}
