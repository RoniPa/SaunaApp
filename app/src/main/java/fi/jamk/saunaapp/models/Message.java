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

import java.util.Date;
import java.util.Locale;

public class Message implements Parcelable {
    private String id;
    private String text;
    private String sender;
    private String target;
    private String saunaId;
    private Date date;

    public Message() {
        this.text = "";
        this.date = new Date();
    }

    public Message(
        String text,
        String sender,
        String target,
        String saunaId,
        Date date,
        @Nullable String id
    ) {
        if (id != null) {
            this.id = id;
        }
        this.text = text;
        this.sender = sender;
        this.target = target;
        this.saunaId = saunaId;
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) { this.sender = sender; }

    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }

    public String getSaunaId() {
        return saunaId;
    }
    public void setSaunaId(String saunaId) {
        this.saunaId = saunaId;
    }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    @Override
    public String toString() {
        String outputFormat = "Message: {\n\tid: %s\n\tsender: %s\n\ttarget: %s\n\tsauna: %s\n\tdate: %s\n\ttext: %s\n}";
        return String.format(
            Locale.ENGLISH,
            outputFormat,
            this.id == null ? "NULL" : this.id,
            this.sender == null ? "NULL" : this.sender,
            this.target == null ? "NULL" : this.target,
            this.saunaId == null ? "NULL" : this.saunaId,
            this.date == null ? "NULL" : this.date.toString(),
            this.text == null ? "NULL" : this.text
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
        dest.writeString(text);
        dest.writeString(sender);
        dest.writeString(target);
        dest.writeString(saunaId);
        dest.writeSerializable(date);
        dest.writeString(id);
    }

    public static final Creator<Message> CREATOR
            = new Creator<Message>() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    private Message(Parcel in) {
        this.text = in.readString();
        this.sender = in.readString();
        this.target = in.readString();
        this.saunaId = in.readString();
        this.date = (Date) in.readSerializable();
        this.id = in.readString();
    }
}
