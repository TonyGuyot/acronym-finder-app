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

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * POJO to represent a given acronym, its expansion and all other related
 * information
 */
public class Acronym implements Parcelable {

    // Attributes
    private final String mName;      // the acronym itself
    private final String mExpansion; // the definition of the acronym
    private final String mComment;   // an optional comment
    private final String mDewey;     // the Dewey code
    private final Date   mAdded;     // when the acronym definition was added on the server

    // Constructors
    private Acronym(String name, String expansion, String comment, String dewey, Date added) {
        mName = name;
        mExpansion = expansion;
        mComment = comment;
        mDewey = dewey;
        mAdded = added;
    }

    // CREATOR object
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Acronym createFromParcel(Parcel in) {
                    return new Acronym(in);
                }

                public Acronym[] newArray(int size) {
                    return new Acronym[size];
                }
            };

    public Acronym(Parcel parcel) {
        mName = parcel.readString();
        mExpansion = parcel.readString();
        mComment = parcel.readString();
        mDewey = parcel.readString();
        mAdded = (Date) parcel.readValue(null);
    }

    // override methods from parcelable interface
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mName);
        parcel.writeString(mExpansion);
        parcel.writeString(mComment);
        parcel.writeString(mDewey);
        parcel.writeValue(mAdded);
    }

    // Getters
    public String getName() {
        return mName;
    }

    public String getExpansion() {
        return mExpansion;
    }

    public String getComment() {
        return mComment;
    }

    public String getDewey() {
        return mDewey;
    }

    public Date getAdded() {
        return mAdded;
    }

    @Override
    public String toString() {
        return mName + ": " + mExpansion;
    }

    // Builder
    public static class Builder {
        private String mName;
        private String mExpansion;
        private String mComment;
        private String mDewey;
        private Date   mAdded;

        public Builder(String name, String expansion) {
            mName = name;
            mExpansion = expansion;
        }

        public Builder comment(String comment) {
            mComment = comment;
            return this;
        }

        public Builder dewey(String dewey) {
            mDewey = dewey;
            return this;
        }

        public Builder added(Date added) {
            mAdded = added;
            return this;
        }

        /**
         * Set the added date field from a String.
         * Expected format is of the form:
         *    "Wed Jan 01 00:00:00 IST 1992"
         *
         * @param added     the added date (as a string)
         * @return          the builder (to enable chain call)
         */
        public Builder added(String added) {
            DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            try {
                mAdded = format.parse(added);
            } catch (ParseException e) {
                mAdded = null;
            }
            return this;
        }

        public Acronym create() {
            return new Acronym(mName, mExpansion, mComment, mDewey, mAdded);
        }
    }
}
