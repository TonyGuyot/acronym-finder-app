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

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * The Utils class is a static class providing several unrelated utility methods.
 */
@SuppressWarnings("unused")
public class Utils {

    // to avoid instantiation
    private Utils() {
        throw new UnsupportedOperationException("Utils is a static class and cannot be instanciated");
    }

    // ------
    // ACCESS TO ANDROID SYSTEM SERVICES

    // get a reference to the input manager
    public static InputMethodManager getInputMethodManager(Activity activity) {
        return (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    // get a reference to the connectivity manager
    public static ConnectivityManager getConnectivityManager(Activity activity) {
        return (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    // ------
    // the following methods are shortcuts to display toasts and snackbars

    // display a toast with a String
    public static void toast(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // display a toast with a resource id
    public static void toast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    // ------ KEYBOARD RELATED METHODS -----

    /**
     * This method is used to hide a keyboard after a user has
     * finished typing in a text box.
     */
    public static void hideKeyboard(Activity activity,
                                    IBinder windowToken) {
        getInputMethodManager(activity).hideSoftInputFromWindow(windowToken, 0);
    }

    // display the keyboard
    public static void showKeyboard(Activity activity) {
        getInputMethodManager(activity).toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    // NETWORK INFORMATION

    // indicates if currently connected to a network
    public boolean isConnectedToNetwork(Activity activity) {
        ConnectivityManager cm = getConnectivityManager(activity);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
