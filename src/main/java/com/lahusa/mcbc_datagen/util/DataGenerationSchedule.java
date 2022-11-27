package com.lahusa.mcbc_datagen.util;

import net.minecraft.server.network.ServerPlayerEntity;

public class DataGenerationSchedule {
    private final ServerPlayerEntity player;
    private final int totalIterations;
    private int remainingIterations;
    private int remainingDelayTicks;
    private boolean isIterationStartRequired;
    private boolean isTeleportConfirmed;
    private boolean hasDelayStarted;
    private boolean isScreenShotRequested;
    private boolean isScreenShotConfirmed;

    public DataGenerationSchedule(ServerPlayerEntity player, int totalIterations) {
        this.player = player;
        this.totalIterations = totalIterations;
        this.remainingIterations = totalIterations;
        this.remainingDelayTicks = 0;
        this.isIterationStartRequired = true;
        this.isTeleportConfirmed = false;
        this.hasDelayStarted = false;
        this.isScreenShotRequested = false;
        this.isScreenShotConfirmed = false;
    }

    public void tick() {
        if(remainingDelayTicks > 0 && !isIterationStartRequired) --remainingDelayTicks;
    }

    public void beginNewIteration() {
        if(remainingIterations == 0) throw new IllegalStateException("There are no remaining iterations, cannot start");

        --remainingIterations;

        isIterationStartRequired = true;
        isTeleportConfirmed = false;
        hasDelayStarted = false;
        isScreenShotRequested = false;
        isScreenShotConfirmed = false;
    }

    public void startDelay(int delayTicks) {
        remainingDelayTicks = delayTicks;
        this.hasDelayStarted = true;
    }

    public void startIteration() {
        this.isIterationStartRequired = false;
    }

    public boolean isDelayElapsed() {
        return hasDelayStarted && remainingDelayTicks == 0;
    }

    public boolean isDone() {
        return remainingIterations == 0;
    }

    public int getElapsedIterations() {
        return totalIterations - remainingIterations;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public int getTotalIterations() {
        return totalIterations;
    }

    public int getRemainingIterations() {
        return remainingIterations;
    }

    public int getRemainingDelayTicks() {
        return remainingDelayTicks;
    }

    public boolean isIterationStartRequired() {
        return isIterationStartRequired;
    }

    public boolean isTeleportConfirmed() {
        return isTeleportConfirmed;
    }

    public void confirmTeleport() {
        isTeleportConfirmed = true;
    }

    public boolean hasDelayStarted() {
        return hasDelayStarted;
    }

    public boolean isScreenShotRequested() {
        return isScreenShotRequested;
    }

    public void setScreenShotRequested(boolean screenShotRequested) {
        isScreenShotRequested = screenShotRequested;
    }

    public boolean isScreenShotConfirmed() {
        return isScreenShotConfirmed;
    }

    public void confirmScreenShot() {
        isScreenShotConfirmed = true;
    }
}
