/*
 * Copyright (C) 2012 The Android Open Source Project
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

package typeofmood.ime.latin.utils;

import android.util.Log;

import typeofmood.ime.latin.define.JniLibName;

public final class JniUtils {
    private static final String TAG = JniUtils.class.getSimpleName();

    public static boolean mHaveGestureLib = false;
    static {
        try {
            System.loadLibrary(JniLibName.JNI_LIB_NAME2);
            mHaveGestureLib = true;
        } catch (UnsatisfiedLinkError ue) {
            try {
                System.loadLibrary(JniLibName.JNI_LIB_NAME);
            } catch (UnsatisfiedLinkError ule) {
                Log.e(TAG, "Could not load native library " + JniLibName.JNI_LIB_NAME, ule);
            }
        }
    }

    private JniUtils() {
        // This utility class is not publicly instantiable.
    }

    public static void loadNativeLibrary() {
        // Ensures the static initializer is called
    }
}
