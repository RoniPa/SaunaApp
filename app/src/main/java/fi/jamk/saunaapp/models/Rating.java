/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.jamk.saunaapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Locale;

public class Rating implements Parcelable {

    private String id;
    private String user;
    private String saunaId;
    private String message;
    private Date time;
    private double rating;

    public Rating() {
        this.rating = 0;
        this.time = new Date();
    }

    public Rating(
        String saunaId,
        String message,
        double rating,
        Date time,
        String user,
        @Nullable String id
    ) {
        if (id != null) {
            this.id = id;
        }
        this.saunaId = saunaId;
        this.message = message;
        this.rating = rating;
        this.time = time;
        this.user = user;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSaunaId() { return saunaId; }
    public void setSaunaId(String saunaId) { this.saunaId = saunaId; }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) { this.rating = rating; }

    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        String outputFormat = "Sauna: {\n\tid: %s\n\tmessage: %s\n\tsaunaId: %s\n\trating: %f\n\ttime: %s\n\tuser: %s\n}";
        return String.format(
                Locale.ENGLISH,
                outputFormat,
                this.id == null ? "NULL" : this.id,
                this.message == null ? "NULL" : this.message,
                this.saunaId == null ? "NULL" : this.saunaId,
                this.rating,
                this.time.toString(),
                this.user == null ? "NULL" : this.user
        );
    }

    /**
     * Parcelable implementation.
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeString(message);
        dest.writeString(saunaId);
        dest.writeSerializable(time);
        dest.writeDouble(rating);
        dest.writeString(id);
    }

    public static final Creator<Rating> CREATOR
            = new Creator<Rating>() {
        public Rating createFromParcel(Parcel in) {
            return new Rating(in);
        }
        public Rating[] newArray(int size) {
            return new Rating[size];
        }
    };

    private Rating(Parcel in) {
        this.user = in.readString();
        this.message = in.readString();
        this.saunaId = in.readString();
        this.time = (Date) in.readSerializable();
        this.rating = in.readDouble();
        this.id = in.readString();
    }
}
