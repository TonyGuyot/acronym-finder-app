package io.github.tonyguyot.acronym;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import io.github.tonyguyot.acronym.data.Acronym;

/**
 * This class provides the actual HTTP-related actions
 * which are possible on an acronym.
 */
public class AcronymHttpMediator {

    // inner class to define a response
    public static class Response {

        // constants for the possible statuses
        public static final int OK = 1;
        public static final int NETWORK_ERROR = 2;
        public static final int PARSE_ERROR = 3;
        public static final int HTTP_ERROR = 4;

        // the general status (see constants above)
        public int mStatus;

        // the HTTP response code
        public int mHttpResponse;

        // the list of retrieved acronyms (or null if error)
        public ArrayList<Acronym> mResults;

        // constructor
        Response() {
            mStatus = OK;
            mHttpResponse = -1;
            mResults = null;
        }
    }

    // Tag for logging information
    private static final String TAG = "AcronymMediator";

    // Url of the acronym server
    public static final String SILMARIL_SERVER = "http://acronyms.silmaril.ie/cgi-bin/uncgi/xaa?";

    // connect to the server to retrieve the list of definitions for the
    // given acronym
    public Response retrieveAcronymDefinitions(String acronym) {
        Response response = new Response();
        try {
            final URL url = new URL(SILMARIL_SERVER + acronym);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                response.mHttpResponse =  conn.getResponseCode();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response.mResults = AcronymXmlParser.parse(in);
            } catch (XmlPullParserException e) {
                Log.d(TAG, "Error: cannot parse XML response.");
                response.mStatus = Response.PARSE_ERROR;
            } catch (IOException e) {
                Log.d(TAG, "Error: did not receive response from server.");
                response.mStatus = Response.HTTP_ERROR;
            } finally {
                conn.disconnect();
            }

        } catch (IOException e) {
            Log.d(TAG, "Error: cannot connect to the server.");
            response.mStatus = Response.NETWORK_ERROR;
        }
        return response;
    }
}
