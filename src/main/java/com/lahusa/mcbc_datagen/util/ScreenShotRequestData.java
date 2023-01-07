package com.lahusa.mcbc_datagen.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ScreenShotRequestData {
    private final String hash;
    private final String biomeGroup;
    private final String biomeID;
    private final long seed;
    private final long timeStamp;
    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemory;


    public ScreenShotRequestData(String hash, String biomeGroup, String biomeID, long seed, long timeStamp, long maxMemory, long totalMemory, long freeMemory) {
        this.biomeGroup = biomeGroup;
        this.biomeID = biomeID;
        this.seed = seed;
        this.timeStamp = timeStamp;
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
        this.hash = hash;
    }

    public ScreenShotRequestData(PacketByteBuf buf) {
        this.hash = buf.readString();
        this.biomeGroup = buf.readString();
        this.biomeID = buf.readString();
        this.seed = buf.readLong();
        this.timeStamp = buf.readLong();
        this.maxMemory = buf.readLong();
        this.totalMemory = buf.readLong();
        this.freeMemory = buf.readLong();
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeString(hash);
        buf.writeString(biomeGroup);
        buf.writeString(biomeID);
        buf.writeLong(seed);
        buf.writeLong(timeStamp);
        buf.writeLong(maxMemory);
        buf.writeLong(totalMemory);
        buf.writeLong(freeMemory);

        return buf;
    }

    public String getScreenShotFileName() {
        return biomeGroup + "-" + hash + ".png";
    }
    public String getMetaDataFileName() {
        return hash + ".json";
    }
    public String getHash() {
        return hash;
    }
    public String getBiomeGroup() {
        return biomeGroup;
    }
    public String getBiomeID() {
        return biomeID;
    }
    public long getSeed() {
        return seed;
    }
    public long getTimeStamp() {
        return timeStamp;
    }
    public long getMaxMemory() {
        return maxMemory;
    }
    public long getTotalMemory() {
        return totalMemory;
    }
    public long getFreeMemory() {
        return freeMemory;
    }
}
