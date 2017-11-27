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
import java.util.Locale;

public class Conversation implements Parcelable {
    private String id;
    private String target;
    private String targetName;

    public Conversation() {
        this.targetName = "";
    }

    public Conversation(
            String target,
            String targetName,
            @Nullable String id
    ) {
        if (id != null) {
            this.id = id;
        }
        this.target = target;
        this.targetName = targetName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTargetName() {
        return targetName;
    }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        String outputFormat = "Conversation: {\n\tid: %s\n\ttarget: %s\n\ttargetName: %s\n}";
        return String.format(
                Locale.ENGLISH,
                outputFormat,
                this.id == null ? "NULL" : this.id,
                this.target == null ? "NULL" : this.target,
                this.targetName == null ? "NULL" : this.targetName
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
        dest.writeString(target);
        dest.writeString(targetName);
        dest.writeString(id);
    }

    public static final Creator<Conversation> CREATOR
            = new Creator<Conversation>() {
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    private Conversation(Parcel in) {
        this.target = in.readString();
        this.targetName = in.readString();
        this.id = in.readString();
    }
}
