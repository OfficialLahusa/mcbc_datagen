package com.lahusa.mcbc_datagen.util;

public class MetaData {
    private float x;
    private float y;
    private float z;
    private float pitch;
    private float yaw;
    private float fov;
    private int time;
    private boolean hudVisible;
    private int selectedSlot;
    //long l = Runtime.getRuntime().maxMemory();
    //long m = Runtime.getRuntime().totalMemory();
    //long n = Runtime.getRuntime().freeMemory();
    private long maxMemoryClient;
    private long totalMemoryClient;
    private long freeMemoryClient;
    private long maxMemoryServer;
    private long totalMemoryServer;
    private long freeMemoryServer;
    //GlDebugInfo.getCpuInfo()
    private String cpuInfo;
    //GlDebugInfo.getVendor()
    private String gpuVendor;
    private int seed;
    private long creationTimeStamp;
    private long requestTimeStamp;
    private String clientHostName;
    private String biomeID;
    private String biomeGroup;
}
