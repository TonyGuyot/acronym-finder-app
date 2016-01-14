/*
 * Copyright (C) 2016 Tony Guyot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.tonyguyot.acronym.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Several static methods which encapsulates function calls which are
 * dependent of the API version used.
 */
@SuppressWarnings("deprecation")
public class CompatUtils {

    /** retrieve a drawable from a resource id */
    public static Drawable getDrawable(Context context, int resourceId) {
        Drawable drawable;
        if (Versions.isLollipopOrHigher()) {
            drawable = context.getDrawable(resourceId);
        } else {
            drawable = context.getResources().getDrawable(resourceId);
        }
        return drawable;
    }
}
