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
import android.util.Log;

import java.util.Locale;

public class Sauna implements Parcelable {
    /**
     * Rating count cap
     */
    public static final int MAX_RATING_COUNT = 500;

    private String id;
    private String description;
    private String name;
    private String photoPath;
    private String owner;

    private double rating;
    /**
     * We use this value to count the effectiveness
     * of any new ratings given to the sauna
     */
    private int ratingCount;

    private double latitude;
    private double longitude;

    public Sauna() {
        this.rating = 0;
        this.ratingCount = 0;
    }

    public Sauna(
            String description,
            String name,
            String photoPath,
            String owner,
            double latitude,
            double longitude,
            double rating,
            int ratingCount,
            @Nullable String id
    ) {
        if (id != null) {
            this.id = id;
        }
        this.description = description;
        this.name = name;
        this.photoPath = photoPath;
        this.owner = owner;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.ratingCount = ratingCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    public String getPhotoPath() {
        return photoPath;
    }
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) {
        if (ratingCount <= Sauna.MAX_RATING_COUNT) {
            this.ratingCount = ratingCount;
        } else {
            Log.d("SaunaModel", "ratingCount value over maximum value, set value as MAX_RATING_COUNT");
            this.ratingCount = Sauna.MAX_RATING_COUNT;
        }
    }

    @Override
    public String toString() {
        String outputFormat = "Sauna: {\n\tid: %s\n\tname: %s\n\tdescription: %s\n\tphotoPath: %s\n\towner: %s\n\tlatitude: %f\n\tlongitude: %f\n}";
        return String.format(
                Locale.ENGLISH,
                outputFormat,
                this.id == null ? "NULL" : this.id,
                this.name == null ? "NULL" : this.name,
                this.description == null ? "NULL" : this.description,
                this.photoPath == null ? "NULL" : this.photoPath,
                this.owner == null ? "NULL" : this.owner,
                this.latitude,
                this.longitude,
                this.rating,
                this.ratingCount
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
        dest.writeString(description);
        dest.writeString(name);
        dest.writeString(photoPath);
        dest.writeString(owner);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(rating);
        dest.writeInt(ratingCount);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<Sauna> CREATOR
            = new Parcelable.Creator<Sauna>() {
        public Sauna createFromParcel(Parcel in) {
            return new Sauna(in);
        }
        public Sauna[] newArray(int size) {
            return new Sauna[size];
        }
    };

    private Sauna(Parcel in) {
        this.description = in.readString();
        this.name = in.readString();
        this.photoPath = in.readString();
        this.owner = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.rating = in.readDouble();
        this.ratingCount = in.readInt();
        this.id = in.readString();
    }
}
