package com.lahusa.mcbc_datagen.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ScreenShotRequestData {
    private String biomeGroup;
    private String biomeID;
    private String hash;

    public ScreenShotRequestData(String biomeGroup, String biomeID, String hash) {
        this.biomeGroup = biomeGroup;
        this.biomeID = biomeID;
        this.hash = hash;
    }

    public ScreenShotRequestData(PacketByteBuf buf) {
        this.biomeGroup = buf.readString();
        this.biomeID = buf.readString();
        this.hash = buf.readString();
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(biomeGroup);
        buf.writeString(biomeID);
        buf.writeString(hash);
        return buf;
    }

    public String getScreenShotFileName() {
        return biomeGroup + "-" + hash + ".png";
    }

    public String getMetaDataFileName() {
        return hash + ".json";
    }

    public String getBiomeGroup() {
        return biomeGroup;
    }

    public void setBiomeGroup(String biomeGroup) {
        this.biomeGroup = biomeGroup;
    }

    public String getBiomeID() {
        return biomeID;
    }

    public void setBiomeID(String biomeID) {
        this.biomeID = biomeID;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
