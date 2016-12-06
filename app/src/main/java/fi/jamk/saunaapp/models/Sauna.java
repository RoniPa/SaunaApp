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

public class Sauna implements Parcelable {

    private String id;
    private String description;
    private String name;
    private String photoUrl;
    private String owner;
    private double latitude;
    private double longitude;

    public Sauna() {}

    public Sauna(String description, String name, String photoUrl, String owner, double latitude, double longitude) {
        this.description = description;
        this.name = name;
        this.photoUrl = photoUrl;
        this.owner = owner;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    /**
     * Parcelable implementation.
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(name);
        dest.writeString(photoUrl);
        dest.writeString(owner);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
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
        this.id = in.readString();
        this.description = in.readString();
        this.name = in.readString();
        this.photoUrl = in.readString();
        this.owner = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }
}
