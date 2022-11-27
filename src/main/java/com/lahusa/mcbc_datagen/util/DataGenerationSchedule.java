package com.lahusa.mcbc_datagen.util;

import net.minecraft.server.network.ServerPlayerEntity;

public class DataGenerationSchedule {
    private final ServerPlayerEntity player;
    private final int totalIterations;
    private int remainingIterations;
    private int remainingDelayTicks;

    private boolean isScreenShotRequested;
    private boolean isScreenShotConfirmed;

    private boolean isIterationStartRequired;

    public DataGenerationSchedule(ServerPlayerEntity player, int totalIterations) {
        this.player = player;
        this.totalIterations = totalIterations;
        this.remainingIterations = totalIterations;
        this.remainingDelayTicks = 0;
        this.isIterationStartRequired = true;
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
        isScreenShotRequested = false;
        isScreenShotConfirmed = false;
    }

    public void start(int delayTicks) {
        remainingDelayTicks = delayTicks;
        this.isIterationStartRequired = false;
    }

    public boolean isDelayElapsed() {
        return remainingDelayTicks == 0;
    }

    public boolean isDone() {
        return isDelayElapsed() && remainingIterations == 0 && !isIterationStartRequired && isScreenShotConfirmed;
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

    public boolean isScreenShotRequested() {
        return isScreenShotRequested;
    }

    public void setScreenShotRequested(boolean screenShotRequested) {
        isScreenShotRequested = screenShotRequested;
    }

    public boolean isScreenShotConfirmed() {
        return isScreenShotConfirmed;
    }

    public void setScreenShotConfirmed(boolean screenShotConfirmed) {
        isScreenShotConfirmed = screenShotConfirmed;
    }

    public boolean isIterationStartRequired() {
        return isIterationStartRequired;
    }
}
