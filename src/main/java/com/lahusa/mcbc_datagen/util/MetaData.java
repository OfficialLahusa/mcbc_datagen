package com.lahusa.mcbc_datagen.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class MetaData {
    // Server Metadata
    private final String biomeID;
    private final String biomeGroup;
    private final long seed;
    private final long requestTimeStamp;
    private final long maxMemoryServer;
    private final long totalMemoryServer;
    private final long freeMemoryServer;

    // Client Metadata
    private final double x;
    private final double y;
    private final double z;
    private final double pitch;
    private final double yaw;
    private final int fov;
    private final long time;
    private final boolean hudVisible;
    private final int selectedSlot;
    private final String mainHandItem;
    private final String offHandItem;
    private final String clientHostName;
    private final long creationTimeStamp;
    private final long maxMemoryClient;
    private final long totalMemoryClient;
    private final long freeMemoryClient;

    public MetaData(ScreenShotRequestData requestData)
    {
        // Extract server metadata from request
        this.biomeID = requestData.getBiomeID();
        this.biomeGroup = requestData.getBiomeGroup();
        this.seed = requestData.getSeed();
        this.requestTimeStamp = requestData.getTimeStamp();
        this.maxMemoryServer = requestData.getMaxMemory();
        this.totalMemoryServer = requestData.getTotalMemory();
        this.freeMemoryServer = requestData.getFreeMemory();

        // Get client metadata
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = Objects.requireNonNull(client.player);
        PlayerInventory inventory = player.getInventory();
        ClientWorld world = Objects.requireNonNull(client.getNetworkHandler()).getWorld();

        Vec3d pos = player.getPos();
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.pitch = player.getPitch();
        this.yaw = player.getYaw();
        this.fov = client.options.getFov().getValue();
        this.time = world.getTime();
        this.hudVisible = client.options.hudHidden;
        this.selectedSlot = inventory.selectedSlot;
        this.mainHandItem = Registry.ITEM.getId(inventory.getMainHandStack().getItem()).toString();
        this.offHandItem = Registry.ITEM.getId(inventory.offHand.get(0).getItem()).toString();
        try {
            this.clientHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        this.creationTimeStamp = System.currentTimeMillis();
        this.maxMemoryClient = Runtime.getRuntime().maxMemory();
        this.totalMemoryClient = Runtime.getRuntime().totalMemory();
        this.freeMemoryClient = Runtime.getRuntime().freeMemory();
    }

    public String getBiomeID() {
        return biomeID;
    }

    public String getBiomeGroup() {
        return biomeGroup;
    }

    public long getSeed() {
        return seed;
    }

    public long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public long getMaxMemoryServer() {
        return maxMemoryServer;
    }

    public long getTotalMemoryServer() {
        return totalMemoryServer;
    }

    public long getFreeMemoryServer() {
        return freeMemoryServer;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public int getFov() {
        return fov;
    }

    public long getTime() {
        return time;
    }

    public boolean isHudVisible() {
        return hudVisible;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public String getClientHostName() {
        return clientHostName;
    }

    public long getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public long getMaxMemoryClient() {
        return maxMemoryClient;
    }

    public long getTotalMemoryClient() {
        return totalMemoryClient;
    }

    public long getFreeMemoryClient() {
        return freeMemoryClient;
    }
}
