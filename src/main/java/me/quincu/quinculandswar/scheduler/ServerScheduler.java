package me.quincu.quinculandswar.scheduler;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class ServerScheduler {

    private static ServerScheduler s = null;

    public static void set(ServerScheduler s) throws IllegalAccessException {
        if (ServerScheduler.s != null){
            throw new IllegalAccessException("Cannot set scheduler more than once!");
        }
        ServerScheduler.s = s;
    }

    public static ServerScheduler get(){
        return s;
    }

    public abstract void runNowRegion(Location location, Runnable runnable);

    public abstract void runLaterRegion(Location location, Runnable runnable, long ticks);

    public abstract void runPeriodRegion(Location location, Runnable runnable, long wait, long period);

    public abstract void runNowEntity(LivingEntity entity, Runnable runnable);

    public abstract void runLaterEntity(LivingEntity entity, Runnable runnable, long ticks);

    public abstract void runPeriodEntity(LivingEntity entity, Runnable runnable, long wait, long period);

    public abstract void runNowAsync(Runnable runnable);

    public abstract CancellableTask runLaterAsync(Runnable runnable, long ticks);

    public abstract CancellableTask runPeriodAsync(Runnable runnable, long wait, long period);

    public abstract void runNowGlobal(Runnable runnable);

    public abstract void runLaterGlobal(Runnable runnable, long ticks);

    public abstract void runPeriodGlobal(Runnable runnable, long wait, long period);

    public void msg(Player player, String s){
        runNowEntity(player, new Runnable() {
            @Override
            public void run() {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
            }
        });
    }

    public abstract void cancelTask(int id);

}
