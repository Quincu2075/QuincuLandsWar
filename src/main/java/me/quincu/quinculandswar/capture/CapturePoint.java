package me.quincu.quinculandswar.capture;

import me.quincu.quinculandswar.QuincuLandsWar;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class CapturePoint {

    private int x;
    private int y;
    private int z;
    private World world;
    private int ticksPassed;
    private int totalTicks;
    private int captureTaskId;
    private boolean captured;
    private BossBar bossBar;
    private boolean active;
    private int invaders;

    private static final long PERIOD = 50L;

    protected CapturePoint(int x, int y, int z, World world, int totalTicks){
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.totalTicks = totalTicks;
        this.captured = false;
        this.bossBar = bossBar();
        this.active = true;
        this.invaders = 5;

        build(true);
        this.captureTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(QuincuLandsWar.t, new Runnable() {
            @Override
            public void run() {
                updateBossBar(bossBar);
                updateInvaders();
                if (getNearbyInvaders() > 0) {
                    addTicksPassed((int) PERIOD);
                }
                if (isActive()){
                    //Location glassLoc = new Location(world, x,y,z).add(0,2,0);
                    //glassLoc.getWorld().getBlockAt(glassLoc).setType(Material.RED_STAINED_GLASS_PANE);
                    if (getProgress() > 1.005){
                        setCaptured(true);
                        unbuild();
                        capture();
                        Bukkit.getScheduler().cancelTask(captureTaskId);
                        captureTaskId = 0;
                    }
                }
            }
        }, PERIOD, PERIOD);
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public boolean equalsToLoc(Location l){
        return l.getBlockX() == x && l.getBlockY() == y && l.getBlockZ() == z && world.getName().equalsIgnoreCase(l.getWorld().getName());
    }

    public float getProgress(){
        return ((float) ticksPassed) / ((float) totalTicks);
    }

    public float getNearbyProgress(){
        return ((float) ticksPassed - PERIOD) / ((float) totalTicks);
    }

    public String bossBarString(){
        int progress = (int)(getProgress() * 100);
        if (progress > 100) progress = 100;
        if (getNearbyInvaders() > 0) {
            return ChatColor.RED + "Enemy is capturing (X: " + x + ", Y: " + y + ", Z: " + z + ") - " + getNearbyInvaders() + " invaders - " + progress + "%";
        }else {
            return ChatColor.GRAY + "Capturing is paused (X: " + x + ", Y: " + y + ", Z: " + z + ") - " + getNearbyInvaders() + " invaders - " + progress + "%";
        }
    }

    public String allyBossBarString(){
        int progress = (int)(getProgress() * 100);
        if (progress > 100) progress = 100;
        if (getNearbyInvaders() > 0) {
            return ChatColor.GREEN + "We are capturing (X: " + x + ", Y: " + y + ", Z: " + z + ") - " + getNearbyInvaders() + " invaders - " + progress + "%";
        }else {
            return ChatColor.GRAY + "Capturing is paused (X: " + x + ", Y: " + y + ", Z: " + z + ") - " + getNearbyInvaders() + " invaders - " + progress + "%";
        }
    }

    private BossBar bossBar(){
        BossBar bb = Bukkit.createBossBar(
                bossBarString(),
                BarColor.RED,
                BarStyle.SOLID
                );
        bb.setProgress(0.0);
        bb.setTitle(bossBarString());
        return bb;
    }

    public BossBar allyBossBar(){
        BossBar bb = Bukkit.createBossBar(
                bossBarString(),
                BarColor.GREEN,
                BarStyle.SOLID
        );
        bb.setProgress(0.0);
        bb.setTitle(allyBossBarString());
        return bb;
    }

    public void updateBossBar(BossBar bossBar){
        float f = getProgress();
        if (f > 1.0) f = 1.0f;
        bossBar.setProgress(f);
        bossBar.setTitle(bossBarString());
        if (getNearbyInvaders() > 0){
            bossBar.setColor(BarColor.PURPLE);
        }else {
            bossBar.setColor(BarColor.RED);
        }
    }

    public void addBossBarPlayer(Player player){
        this.bossBar.addPlayer(player);
    }

    public void clearBossBarPlayers(){
        this.bossBar.removeAll();
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public int getNearbyInvaders(){
        return invaders;
    }

    public void updateInvaders(){

    }

    public void setInvaders(int invaders) {
        this.invaders = invaders;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public int getTicksPassed() {
        return ticksPassed;
    }

    public void setTicksPassed(int ticksPassed) {
        this.ticksPassed = ticksPassed;
    }

    public void setTotalTicks(int totalTicks) {
        this.totalTicks = totalTicks;
    }

    public void addTicksPassed(int ticksPassed){
        this.ticksPassed += ticksPassed;
    }

    public boolean contains(Location loc){
        //if (loc.getY() == y + 2) return false;
        for (Location l : getLocs()){
            //if (!l.getWorld().getName().equalsIgnoreCase(loc.getWorld().getName())) continue;
            if (l.getX() == loc.getX() && l.getY() == loc.getY() && l.getZ() == loc.getZ()) return true;
        }
        return false;
    }

    private Location[] getLocs(){
        Location l = new Location(world, x,y,z);
        Location[] locs = new Location[]{
                l.clone().add(-1, 0, 1),
                l.clone().add(-1, 0, 0),
                l.clone().add(-1, 0, -1),
                l.clone().add(0, 0, -1),
                l.clone().add(0, 0, 0),
                l.clone().add(0, 0, 1),
                l.clone().add(1, 0, 1),
                l.clone().add(1, 0, 0),
                l.clone().add(1, 0, -1),
                l.clone().add(0, 1, 0),
                l.clone().add(0, 2, 0),
        };
        return locs;
    }

    public void deleteReboot(){
        clearBossBarPlayers();
        for (Location l : getLocs()){
            l.getBlock().setType(Material.AIR);
        }
    }

    //true = build
    //false = undo the build
    public void build(boolean on){
        Location[] locs = getLocs();
        int i = 0;
        if (!on){
            Bukkit.getScheduler().runTask(QuincuLandsWar.t, new Runnable() {
                @Override
                public void run() {
                    Location last = locs[locs.length - 1];
                    last.getBlock().setType(Material.AIR);
                }
            });
        }
        for (Location loc : locs){
            Material material = Material.IRON_BLOCK;
            if (!on){
                material = Material.AIR;
            }else {
                if (loc.getY() == y + 1) {
                    material = Material.BEACON;
                } else if (loc.getY() == y + 2) {
                    material = Material.RED_STAINED_GLASS_PANE;
                }
            }

            final Material finalMaterial = material;
            Bukkit.getScheduler().runTaskLater(QuincuLandsWar.t, new Runnable() {
                @Override
                public void run() {
                    loc.getWorld().getBlockAt(loc).setType(finalMaterial);
                    loc.getWorld().playSound(loc, Sound.BLOCK_WOOD_PLACE, 5.0f, 1.0f);
                }
            }, i);

            i += 3;
        }
        if (!on) {
            Bukkit.getScheduler().runTaskLater(QuincuLandsWar.t, new Runnable() {
                @Override
                public void run() {
                    r();
                }
            }, i + 1);
        }
    }

    private void r(){
        QuincuLandsWar.t.getCaptureBlockManager().remove(this);
    }

    public boolean isCaptured() {
        return captured;
    }

    public void finalBossBarUpdate(){
        this.bossBar.setTitle("Enemy Captured: " + isCaptured());
    }


    public void unbuild(){
        build(false);
        finalBossBarUpdate();
        Bukkit.getScheduler().runTaskLater(QuincuLandsWar.t, new Runnable() {
            @Override
            public void run() {
                clearBossBarPlayers();
            }
        }, 100);
        Bukkit.getScheduler().cancelTask(captureTaskId);
        this.captureTaskId = 0;
    }

    @Override
    public String toString(){
        return "CapturePoint-" + x + ":" + y + ":" + z;
    }

    public void capture(){
        bossBar.setProgress(1.0f);
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Capture point at (" + x + "," + y + "," + z + ") has been captured!");
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
        this.active = false;
    }

    protected void mine(){
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Capture point at (" + x + "," + y + "," + z + ") has been mined!");
    }

    public boolean isActive(){
        return active;
    }
}
