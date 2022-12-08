package com.lahusa.mcbc_datagen.util;

import net.minecraft.server.network.ServerPlayerEntity;

public class DataGenerationSchedule {
    private final ServerPlayerEntity player;
    private final int totalIterations;
    private int elapsedIterations;
    private int totalScreenShots;
    private int capturedScreenShots;
    private int remainingDelayTicks;
    private State state;

    public DataGenerationSchedule(ServerPlayerEntity player, int totalIterations) {
        this.player = player;
        this.totalIterations = totalIterations;
        this.elapsedIterations = 0;
        this.totalScreenShots = 0; // is properly set in DataGenerationManager.randomizePosition()
        this.capturedScreenShots = 0;
        this.remainingDelayTicks = 0;
        this.state = State.SCHED_INIT;
    }

    public void tick() {
        if(remainingDelayTicks > 0) --remainingDelayTicks;
    }

    public void beginNewIteration() {
        if(elapsedIterations == totalIterations) throw new IllegalStateException("There are no remaining iterations, cannot start");
        ++elapsedIterations;
        capturedScreenShots = 0;
    }

    public void startDelay(int delayTicks) {
        remainingDelayTicks = delayTicks;
    }

    public boolean isDelayElapsed() {
        return remainingDelayTicks == 0;
    }

    public boolean isDone() {
        return elapsedIterations == totalIterations;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getTotalIterations() {
        return totalIterations;
    }

    public int getElapsedIterations() {
        return elapsedIterations;
    }

    public int getTotalScreenShots() {
        return totalScreenShots;
    }

    public void setTotalScreenShots(int totalScreenShots) {
        this.totalScreenShots = totalScreenShots;
    }

    public int getCapturedScreenShots() {
        return capturedScreenShots;
    }

    public void setCapturedScreenShots(int capturedScreenShots) {
        this.capturedScreenShots = capturedScreenShots;
    }

    public int getRemainingDelayTicks() {
        return remainingDelayTicks;
    }

    public enum State {
        SCHED_INIT,
        ITER_INIT,
        AWAIT_TP_CONFIRMATION,
        AWAIT_GEN_DELAY,
        RANDOMIZATION,
        AWAIT_RAND_DELAY,
        AWAIT_SCREENSHOT_CONF
    }
}
