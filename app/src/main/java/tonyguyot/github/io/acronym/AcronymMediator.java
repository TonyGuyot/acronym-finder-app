package tonyguyot.github.io.acronym;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import tonyguyot.github.io.acronym.data.Acronym;

/**
 * This class provides the actions which are possible on an acronym.
 */
public class AcronymMediator {

    // Tag for logging information
    private static final String TAG = "AcronymMediator";

    // Url of the acronym server
    public static final String SILMARIL_SERVER = "http://acronyms.silmaril.ie/cgi-bin/uncgi/xaa?";

    // connect to the server to retrieve the list of definitions for the
    // given acronym
    public ArrayList<Acronym> retrieveAcronymDefinitions(String acronym) {
        ArrayList<Acronym> acronyms = null;
        try {
            final URL url = new URL(SILMARIL_SERVER + acronym);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                acronyms = AcronymXmlParser.parse(in);
            } catch (XmlPullParserException e) {
                Log.d(TAG, "Error: cannot parse XML response.");
                // TODO
            } catch (IOException e) {
                Log.d(TAG, "Error: did not receive response from server.");
                // TODO
            } finally {
                conn.disconnect();
            }

        } catch (IOException e) {
            Log.d(TAG, "Error: cannot connect to the server.");
            // TODO
        }
        return acronyms;
    }
}
