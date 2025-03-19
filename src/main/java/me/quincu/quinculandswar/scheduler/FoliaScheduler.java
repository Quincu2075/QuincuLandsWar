package me.quincu.quinculandswar.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.quincu.quinculandswar.QuincuLandsWar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class FoliaScheduler extends ServerScheduler {

    private HashMap<Integer, CancellableTask> tasks;
    private int currentId;

    public FoliaScheduler(){
        tasks = new HashMap<>();
        currentId = 0;
    }

    private int getNextId(){
        currentId = currentId + 1;
        return this.currentId;
    }

    @Override
    public void runNowRegion(Location location, Runnable runnable) {
        Bukkit.getRegionScheduler().run(QuincuLandsWar.t, location, scheduledTask -> {
            runnable.run();
        });
    }

    @Override
    public void runLaterRegion(Location location, Runnable runnable, long ticks) {
        Bukkit.getRegionScheduler().runDelayed(QuincuLandsWar.t, location, scheduledTask -> {
            runnable.run();
        }, ticks);
    }

    @Override
    public void runPeriodRegion(Location location, Runnable runnable, long wait, long period) {
        Bukkit.getRegionScheduler().runAtFixedRate(QuincuLandsWar.t, location, scheduledTask -> {
            runnable.run();
        }, wait, period);
    }

    @Override
    public void runNowEntity(LivingEntity entity, Runnable runnable) {
        entity.getScheduler().run(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        }, null);
    }

    @Override
    public void runLaterEntity(LivingEntity entity, Runnable runnable, long ticks) {
        entity.getScheduler().runDelayed(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        }, null, ticks);
    }

    @Override
    public void runPeriodEntity(LivingEntity entity, Runnable runnable, long wait, long period) {
        entity.getScheduler().runAtFixedRate(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        }, null, wait, period);
    }

    @Override
    public void runNowAsync(Runnable runnable) {
        Bukkit.getAsyncScheduler().runNow(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        });
    }

    @Override
    public CancellableTask runLaterAsync(Runnable runnable, long wait) {

        long ms = wait * 50;

        ScheduledTask st = Bukkit.getAsyncScheduler().runDelayed(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        }, ms, TimeUnit.MILLISECONDS);
        int id = getNextId();
        CancellableTask ct = new CancellableTask() {
            @Override
            public void cancel() {
                st.cancel();
            }

            @Override
            public int taskId() {
                return getNextId();
            }
        };
        tasks.put(id, ct);
        return ct;
    }

    @Override
    public CancellableTask runPeriodAsync(Runnable runnable, long wait, long period) {

        long msWait = wait * 50;
        long msPeriod = period * 50;

        ScheduledTask st = Bukkit.getAsyncScheduler().runAtFixedRate(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        }, msWait, msPeriod, TimeUnit.MILLISECONDS);
        int id = getNextId();
        CancellableTask ct = new CancellableTask() {
            @Override
            public void cancel() {
                st.cancel();
            }

            @Override
            public int taskId() {
                return getNextId();
            }
        };
        tasks.put(id, ct);
        return ct;
    }

    @Override
    public void runNowGlobal(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        });
    }

    @Override
    public void runLaterGlobal(Runnable runnable, long ticks) {
        Bukkit.getGlobalRegionScheduler().runDelayed(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        }, ticks);
    }

    @Override
    public void runPeriodGlobal(Runnable runnable, long wait, long period) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(QuincuLandsWar.t, scheduledTask -> {
            runnable.run();
        }, wait, period);
    }

    @Override
    public void cancelTask(int id) {
        tasks.get(id).cancel();
    }

}
