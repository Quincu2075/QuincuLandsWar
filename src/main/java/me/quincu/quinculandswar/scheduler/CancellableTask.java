package me.quincu.quinculandswar.scheduler;

public interface CancellableTask {

    void cancel();

    int taskId();

}
