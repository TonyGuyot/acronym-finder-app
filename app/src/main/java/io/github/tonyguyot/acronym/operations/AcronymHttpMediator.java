package io.github.tonyguyot.acronym.operations;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.github.tonyguyot.acronym.AcronymXmlParser;
import io.github.tonyguyot.acronym.data.AcronymList;

/**
 * This class provides the actual HTTP-related actions
 * which are possible on an acronym.
 */
public class AcronymHttpMediator {

    // Tag for logging information
    private static final String TAG = "AcronymMediator";

    // Url of the acronym server
    public static final String SILMARIL_SERVER = "http://acronyms.silmaril.ie/cgi-bin/uncgi/xaa?";

    // connect to the server to retrieve the list of definitions for the
    // given acronym
    public AcronymList retrieveFromServer(String acronym) {
        AcronymList response = new AcronymList();
        try {
            final URL url = new URL(SILMARIL_SERVER + acronym);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                response.setAdditionalStatus(conn.getResponseCode());
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response.setContent(AcronymXmlParser.parse(in));
            } catch (XmlPullParserException e) {
                Log.d(TAG, "Error: cannot parse XML response.");
                response.setStatus(AcronymList.Status.STATUS_ERROR_PARSING);
            } catch (IOException e) {
                Log.d(TAG, "Error: did not receive response from server.");
                response.setStatus(AcronymList.Status.STATUS_ERROR_COMMUNICATION);
            } finally {
                conn.disconnect();
            }

        } catch (IOException e) {
            Log.d(TAG, "Error: cannot connect to the server.");
            response.setStatus(AcronymList.Status.STATUS_ERROR_NETWORK);
        }
        return response;
    }
}
