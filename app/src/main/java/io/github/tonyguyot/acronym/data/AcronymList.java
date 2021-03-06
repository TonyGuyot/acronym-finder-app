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
package io.github.tonyguyot.acronym.data;

import java.util.ArrayList;

/**
 * POJO to represent a list of acronyms, and in case the list is empty, some
 * information about why the list could not be retrieved.
 */
public class AcronymList {

    // possible values for the status:
    //
    public static class Status {

        // everything OK
        public static final int STATUS_OK = 0;

        // could not connect to server
        public static final int STATUS_ERROR_NETWORK = -1;

        // could connect to server but could not receive reply
        public static final int STATUS_ERROR_COMMUNICATION = -2;

        // could not retrieve information due to system problem
        public static final int STATUS_ERROR_SYSTEM = -3;

        // retrieve information but could not interpret it
        public static final int STATUS_ERROR_PARSING = -4;

        // some other kind of error occurred
        @SuppressWarnings("unused")
        public static final int STATUS_ERROR_OTHER = -5;

        // the query data were invalid
        public static final int STATUS_INVALID_DATA = -6;
    }


    // the list of acronyms (may be empty)
    // or null if the list could not be retrieved
    private ArrayList<Acronym> mContent;

    // status of the list: OK or ERROR
    private int mStatus;

    // for some errors, more information may be required
    // for example, in case of communication error, contains the HTTP
    // response code
    private int mAdditionalStatus;

    // the date when the information has been retrieved from the server
    private long mRetrievedDate;

    // indicate that the retrieved content is considered as no more valid
    // (when true)
    private boolean mIsExpired;


    // default constructor
    public AcronymList() {
        mContent = null;
        mStatus = Status.STATUS_OK;
        mAdditionalStatus = -1;
        mRetrievedDate = 0L;
        mIsExpired = false;
    }

    // getters and setters:
    //

    public ArrayList<Acronym> getContent() {
        return mContent;
    }

    public void setContent(ArrayList<Acronym> content) {
        mContent = content;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getAdditionalStatus() {
        return mAdditionalStatus;
    }

    public void setAdditionalStatus(int additionalStatus) {
        mAdditionalStatus = additionalStatus;
    }

    @SuppressWarnings("unused")
    public long getRetrievedDate() {
        return mRetrievedDate;
    }

    @SuppressWarnings("unused")
    public void setRetrievedDate(long retrievedDate) {
        mRetrievedDate = retrievedDate;
    }

    public boolean isExpired() {
        return mIsExpired;
    }

    public void setAsExpired() {
        mIsExpired = true;
    }

    @SuppressWarnings("unused")
    public void setAsNotExpired() {
        mIsExpired = false;
    }
}
