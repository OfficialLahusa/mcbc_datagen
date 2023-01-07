package com.lahusa.mcbc_datagen.util;

public class MetaData {
    // Server Metadata
    private String biomeID;
    private String biomeGroup;
    private int seed;
    private long requestTimeStamp;
    private long maxMemoryServer; //Runtime.getRuntime().maxMemory();
    private long totalMemoryServer; //Runtime.getRuntime().totalMemory();
    private long freeMemoryServer; //Runtime.getRuntime().freeMemory();

    // Client Metadata
    private float x;
    private float y;
    private float z;
    private float pitch;
    private float yaw;
    private float fov;
    private int time;
    private boolean hudVisible;
    private int selectedSlot;
    private String clientHostName;
    private String cpuInfo; //GlDebugInfo.getCpuInfo()
    private String gpuVendor; //GlDebugInfo.getVendor()
    private long creationTimeStamp;
    private long maxMemoryClient; //Runtime.getRuntime().maxMemory();
    private long totalMemoryClient; //Runtime.getRuntime().totalMemory();
    private long freeMemoryClient; //Runtime.getRuntime().freeMemory();

    public MetaData()
    {

    }

    public void setServerSideData(ScreenShotRequestData requestData) {
        this.biomeGroup = requestData.getBiomeGroup();
        this.biomeID = requestData.getBiomeID();
    }
}
