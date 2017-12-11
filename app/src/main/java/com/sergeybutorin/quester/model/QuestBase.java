package com.sergeybutorin.quester.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sergeybutorin on 09/12/2017.
 */

public class QuestBase implements Serializable {
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
}
