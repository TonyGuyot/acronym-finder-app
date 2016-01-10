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
package io.github.tonyguyot.acronym.operations;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.github.tonyguyot.acronym.data.AcronymList;

/**
 * This class provides the actual HTTP-related actions
 * which are possible on an acronym.
 */
public class AcronymHttpMediator {

    // Tag for logging information
    private static final String TAG = "AcronymMediator";

    // Url of the acronym server
    public static final String SILMARIL_SERVER = "http://acronyms.silmaril.ie/cgi-bin/xaa?";

    // connect to the server to retrieve the list of definitions for the
    // given acronym
    public AcronymList retrieveFromServer(String acronym) {
        AcronymList response = new AcronymList();
        try {
            final URL url = new URL(SILMARIL_SERVER + acronym);
            Log.d(TAG, "fetching " + url.toString());
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
