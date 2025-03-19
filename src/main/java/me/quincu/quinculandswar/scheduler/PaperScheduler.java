package me.quincu.quinculandswar.scheduler;

import me.quincu.quinculandswar.QuincuLandsWar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PaperScheduler extends ServerScheduler{

    private ScheduledExecutorService ses;

    public PaperScheduler(){
        ses = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void runNowRegion(Location location, Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runLaterRegion(Location location, Runnable runnable, long ticks) {
        Bukkit.getScheduler().runTaskLater(QuincuLandsWar.t, runnable, ticks);
    }

    @Override
    public void runPeriodRegion(Location location, Runnable runnable, long wait, long period) {
        Bukkit.getScheduler().runTaskTimer(QuincuLandsWar.t, runnable, wait, period);
    }

    @Override
    public void runNowEntity(LivingEntity entity, Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runLaterEntity(LivingEntity entity, Runnable runnable, long ticks) {
        Bukkit.getScheduler().runTaskLater(QuincuLandsWar.t, runnable, ticks);
    }

    @Override
    public void runPeriodEntity(LivingEntity entity, Runnable runnable, long wait, long period) {
        Bukkit.getScheduler().runTaskTimer(QuincuLandsWar.t, runnable, wait, period);
    }

    @Override
    public void runNowAsync(Runnable runnable) {
        ses.execute(runnable);
    }

    @Override
    public CancellableTask runLaterAsync(Runnable runnable, long wait) {
        BukkitTask future = Bukkit.getScheduler().runTaskLaterAsynchronously(QuincuLandsWar.t, runnable, wait);
        return new CancellableTask() {
            @Override
            public void cancel() {
                future.cancel();
            }

            @Override
            public int taskId() {
                return future.getTaskId();
            }
        };
    }

    @Override
    public CancellableTask runPeriodAsync(Runnable runnable, long wait, long period) {
        BukkitTask future = Bukkit.getScheduler().runTaskTimerAsynchronously(QuincuLandsWar.t, runnable, wait, period);
        return new CancellableTask() {
            @Override
            public void cancel() {
                future.cancel();
            }

            @Override
            public int taskId() {
                return future.getTaskId();
            }
        };
    }

    @Override
    public void runNowGlobal(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runLaterGlobal(Runnable runnable, long ticks) {
        Bukkit.getScheduler().runTaskLater(QuincuLandsWar.t, runnable, ticks);
    }

    @Override
    public void runPeriodGlobal(Runnable runnable, long wait, long period) {
        Bukkit.getScheduler().runTaskTimer(QuincuLandsWar.t, runnable, wait, period);
    }

    @Override
    public void cancelTask(int id) {
        Bukkit.getScheduler().cancelTask(id);
    }

}
