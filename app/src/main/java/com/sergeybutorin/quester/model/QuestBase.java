package com.sergeybutorin.quester.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import static com.sergeybutorin.quester.utils.Common.bytesToUuid;
import static com.sergeybutorin.quester.utils.Common.uuidToBytes;

/**
 * Created by sergeybutorin on 09/12/2017.
 */

public class QuestBase implements Parcelable {
    private UUID uuid;
    private int version;

    public QuestBase() {
    }

    public QuestBase(UUID uuid) {
        this.uuid = uuid;
    }

    public QuestBase(UUID uuid, int version) {
        this.uuid = uuid;
        this.version = version;
    }

    public static final Parcelable.Creator<QuestBase> CREATOR
            = new Parcelable.Creator<QuestBase>() {
        public QuestBase createFromParcel(Parcel in) {
            return new QuestBase(in);
        }

        public QuestBase[] newArray(int size) {
            return new QuestBase[size];
        }
    };

    private QuestBase(Parcel in) {
        byte [] bb = new byte[16];
        in.readByteArray(bb);
        this.uuid = bytesToUuid(bb);
        this.version = in.readInt();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(uuidToBytes(uuid));
        dest.writeInt(version);
    }
}
