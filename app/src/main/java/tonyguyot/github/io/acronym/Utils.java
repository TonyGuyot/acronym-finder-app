package tonyguyot.github.io.acronym;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * The Utils class is a static class providing several unrelated utility methods.
 */
public class Utils {

    // to avoid instantiation
    private Utils() {
        throw new UnsupportedOperationException("Utils is a static class and cannot be instanciated");
    }

    // ------
    // the following methods are shortcuts to retrieve different services
    public static InputMethodManager getInputMethodManager(Activity activity) {
        return (InputMethodManager) activity.getSystemService
                (Context.INPUT_METHOD_SERVICE);
    }
    // ------

    // display a toast with a String
    public static void toast(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // display a toast with a resource id
    public static void toast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    // display a snackbar
    // TODO


    /**
     * This method is used to hide a keyboard after a user has
     * finished typing in a text box.
     */
    public static void hideKeyboard(Activity activity,
                                    IBinder windowToken) {
        getInputMethodManager(activity).hideSoftInputFromWindow(windowToken, 0);
    }

}
