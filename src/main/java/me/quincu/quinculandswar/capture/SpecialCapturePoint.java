package me.quincu.quinculandswar.capture;

import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.memberholder.MemberHolder;
import me.angeschossen.lands.api.player.LandPlayer;
import me.angeschossen.lands.api.war.War;
import me.angeschossen.lands.api.war.enums.WarTeam;
import me.angeschossen.lands.api.war.player.WarPlayer;
import me.quincu.quinculandswar.QuincuLandsWar;
import me.quincu.quinculandswar.util.PlayerUtils;
import me.quincu.quinculandswar.util.StringUtil;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class SpecialCapturePoint extends CapturePoint{

    private War war;
    private WarTeam warTeam;
    private int extraTask;
    private Location capLoc;
    private Chunk baseChunk;
    private int cachedInvaders;
    private BossBar allyBossBar;
    private boolean mined;
    private BossBar nearbyBossBar;
    private int fireworkTask;

    protected SpecialCapturePoint(int x, int y, int z, World world, int totalTicks, War war, WarTeam warTeam) {
        super(x, y, z, world, totalTicks);
        this.war = war;
        this.warTeam = warTeam;
        this.capLoc = new Location(getWorld(), getX(), getY(), getZ());
        this.baseChunk = capLoc.getChunk();
        this.cachedInvaders = 1;
        this.allyBossBar = allyBossBar();
        this.mined = false;
        this.nearbyBossBar = nearbyBossBar();
        updateNearbyBossBar();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (PlayerUtils.getPlayerWarList(getEnemy()).contains(player.getUniqueId())) {
                getBossBar().addPlayer(player);
            } else if (PlayerUtils.getPlayerWarList(getAlly()).contains(player.getUniqueId())) {
                allyBossBar.addPlayer(player);
            }
        }

        sendCapturePointMessage("placed in our land: %land%, " + x + ", " + y + ", " + z, false);
        extraTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(QuincuLandsWar.t, new Runnable() {
            @Override
            public void run() {
                updateNearbyBossBar();
                if (war == null) {
                    unbuild();
                    return;
                }
                if (war.isEndingSoon()) unbuild();
                Land land = QuincuLandsWar.t.getLands().getLandByChunk(baseChunk.getWorld(), baseChunk.getX(), baseChunk.getZ());
                if (land == null) {
                    unbuild();
                }
                cachedInvaders = getNearbyInvadersMethod();
            }
        }, 5, 5);

        launchFirework(FireworkEffect.builder().withColor(Color.RED).trail(true).build());
        this.fireworkTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(QuincuLandsWar.t, new Runnable() {
            @Override
            public void run() {
                launchFirework(FireworkEffect.builder().withColor(Color.RED).trail(false).build());
            }
        }, 300L, 300L);
    }

    public void launchFirework(FireworkEffect fireworkEffect) {
        Firework fw = (Firework) capLoc.getWorld().spawn(capLoc.clone().add(0,2.5,0), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(fireworkEffect);
        fw.setFireworkMeta(meta);
        //use meta to customize the firework or add parameters to the method
        fw.setVelocity(new Vector(0, 0.12, 0));
        //speed is how fast the firework flies
    }

    private BossBar nearbyBossBar(){
        BossBar bb = Bukkit.createBossBar(
                ChatColor.RED + "Nearby Capture Point - 1 invader - 0%",
                BarColor.PURPLE,
                BarStyle.SOLID
        );
        bb.setProgress(0.0);
        return bb;
    }

    private void updateNearbyBossBar(){
        float p = getNearbyProgress();
        if (p > 1.0) p = 1.0f;
        if (p < 0.0) p = 0.0f;
        nearbyBossBar.setProgress(p);
        nearbyBossBar.setTitle(nearbyBossBarString());

        Chunk mainChunk = capLoc.getChunk();

        for (Player playa : nearbyBossBar.getPlayers()){
            Chunk playaChunk = playa.getChunk();

            if (mainChunk.getX() == playaChunk.getX() && mainChunk.getZ() == playaChunk.getZ()) continue;

            nearbyBossBar.removePlayer(playa);

            if (PlayerUtils.getPlayerWarList(getEnemy()).contains(playa.getUniqueId())){
                getBossBar().addPlayer(playa);
            }else if (PlayerUtils.getPlayerWarList(getAlly()).contains(playa.getUniqueId())){
                allyBossBar.addPlayer(playa);
            }
        }

        for (Entity e : mainChunk.getEntities()){
            if (!(e instanceof Player)) continue;
            Player player = (Player) e;

            nearbyBossBar.addPlayer(player);

            if (allyBossBar.getPlayers().contains(player)){
                allyBossBar.removePlayer(player);
            }else if (getBossBar().getPlayers().contains(player)){
                getBossBar().removePlayer(player);
            }
        }
    }

    public String nearbyBossBarString(){
        int progress = (int)(getNearbyProgress() * 100);
        if (progress > 100) progress = 100;
        if (progress < 0) progress = 0;
        return ChatColor.RED + "Nearby Capture Point - " + getNearbyInvaders() + " invaders - " + progress + "%";
    }

    @Override
    public void updateBossBar(BossBar bossBar){
        float f = getNearbyProgress();
        if (f > 1.0) f = 1.0f;
        if (f < 0.0) f = 0.0f;
        bossBar.setProgress(f);
        bossBar.setTitle(bossBarString());
        allyBossBar.setProgress(f);
        allyBossBar.setTitle(allyBossBarString());
    }

    private Land getEnemy(){
        if (warTeam == WarTeam.DEFENDER){
            return QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }else {
            return QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        }
    }

    private Land getAlly(){
        if (warTeam == WarTeam.DEFENDER){
            return QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        }else {
            return QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }
    }

    private void sendCapturePointMessage(String msg, boolean enemyFavor){
        for (UUID uuid : PlayerUtils.getPlayerWarList(getEnemyLand())){
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            msg = msg.replaceAll("%land%", ChatColor.stripColor(getEnemy().getName()));

            if (!enemyFavor) {
                player.sendTitle(ChatColor.RED + "Capture Point", ChatColor.RED + msg);
            }else {
                player.sendTitle(ChatColor.GREEN + "Capture Point", ChatColor.GREEN + msg);
            }
        }
        for (UUID uuid : PlayerUtils.getPlayerWarList(getAlly())){
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            msg = msg.replaceAll("%land%", ChatColor.stripColor(getEnemy().getName()));
            msg = msg.replaceAll("our land", "enemy land");
            if (!enemyFavor) {
                player.sendTitle(ChatColor.GREEN + "Capture Point", ChatColor.GREEN + msg);
            }else {
                player.sendTitle(ChatColor.RED + "Capture Point", ChatColor.RED + msg);
            }
        }
    }

    @Override
    public int getNearbyInvaders() {
        return cachedInvaders;
    }

    @Override
    public void updateInvaders() {
        setInvaders(getNearbyInvadersMethod());
    }

    public int getNearbyInvadersMethod() {
        ArrayList<Chunk> chunks = new ArrayList<>();
        Land land = QuincuLandsWar.t.getLands().getLandByChunk(getWorld(), baseChunk.getX(), baseChunk.getZ());
        if (land == null) return 0;
        for (int i1 = -2; i1 < 3; i1++){
            for (int i2 = -2; i2 < 3; i2++){
                if (land.hasChunk(getWorld(), baseChunk.getX() + i1, baseChunk.getZ() + i2)){
                    chunks.add(getWorld().getChunkAt(baseChunk.getX() + i1, baseChunk.getZ() + i2));
                }
            }
        }
        int i = 0;
        for (Chunk c : chunks){
            for (Entity e : c.getEntities()){
                if (e instanceof Player){
                    Player player = (Player) e;
                    if (getAlly().getTrustedPlayers().contains(player.getUniqueId())) i++;
                }
            }
        }
        return i;
    }

    public void broken(){
        sendCapturePointMessage("Removed from %land%", true);
        this.nearbyBossBar.setTitle(ChatColor.RED + "Nearby Capture Point Destroyed!");
    }

    @Override
    public void capture() {
        getBossBar().setProgress(1.0f);
        allyBossBar.setProgress(1.0f);
        nearbyBossBar.setProgress(1.0f);
        war.getStats(warTeam).setCaptures(war.getStats(warTeam).getCaptures() + 1);
        Location loc = new Location(getWorld(), getX(), getY(), getZ());
        Chunk c = loc.getChunk();
        int cX = c.getX();
        int cZ = c.getZ();

        Land enemy = QuincuLandsWar.t.getLands().getLandByName(getEnemy().getName());
        int unclaimed = 0;
        for (int i1 = -1; i1 < 2; i1++){
            for (int i2 = -1; i2 < 2; i2++){
                int getCX = cX + i1;
                int getCZ = cZ + i2;

                if (enemy.hasChunk(getWorld(), getCX, getCZ)){
                    unclaimed++;
                    enemy.unclaimChunk(getWorld(), getCX, getCZ, null);
                }
            }
        }

        msgAllies("Our capture point has been captured! &a(+" + CapturePointManager.CAPTURE_POINTS + " pts)");
        msgEnemies("Our enemies have captured a capture point!");

        sendCapturePointMessage("Captured " + unclaimed + " chunk(s) from %land%", false);
        this.nearbyBossBar.setTitle(ChatColor.RED + "Nearby Capture Point Success! - 100%");
    }

    @Override
    protected void mine() {
        if (!isActive()) return;
        if (this.mined) return;
        mined = true;
        WarTeam opposing = warTeam.getOpposite();
        war.getStats(opposing).modifyPoints(CapturePointManager.MINE_POINTS);
        this.nearbyBossBar.setTitle(ChatColor.RED + "Nearby Capture Point Destroyed!");
        msgAllies("Our capture point has been mined by the enemy!");
        msgEnemies("We have mined an enemy capture point. &a(+" + CapturePointManager.MINE_POINTS + " pts)");
    }

    @Override
    public void unbuild() {
        super.unbuild();
        Bukkit.getScheduler().cancelTask(extraTask);
        Bukkit.getScheduler().cancelTask(fireworkTask);
    }



    @Override
    public void clearBossBarPlayers() {
        getBossBar().removeAll();
        this.allyBossBar.removeAll();
        this.nearbyBossBar.removeAll();
    }

    @Override
    public void finalBossBarUpdate() {
        String titleAlly = null;
        String titleEnemy = null;

        if (!isCaptured()){
            titleAlly = StringUtil.color("&c&lCapture Point Removed");
            titleEnemy = StringUtil.color("&a&lCapture Point Removed");
            getBossBar().setColor(BarColor.GREEN);
            this.allyBossBar.setColor(BarColor.RED);

        }else {
            titleAlly = StringUtil.color("&a&lWe have captured!");
            titleEnemy = StringUtil.color("&c&lEnemy has captured!");
            getBossBar().setColor(BarColor.RED);
            this.allyBossBar.setColor(BarColor.GREEN);
        }
        this.allyBossBar.setTitle(titleAlly);
        getBossBar().setTitle(titleEnemy);
    }

    public WarTeam getWarTeam() {
        return warTeam;
    }

    private Land getEnemyLand(){
        if (warTeam == WarTeam.DEFENDER){
            return QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }else {
            return QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        }
    }

    private Land getAllyLand(){
        if (warTeam == WarTeam.ATTACKER){
            return QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }else {
            return QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        }
    }

    private void msgAllies(String msg){

        for (UUID uuid : PlayerUtils.getPlayerWarList(getAllyLand())){
            Player player = Bukkit.getPlayer(uuid);
            if (player != null){
                player.sendMessage(StringUtil.color("&8[&4&lWAR&8] &f" + msg));
            }
        }

    }

    private void msgEnemies(String msg){

        for (UUID uuid : PlayerUtils.getPlayerWarList(getEnemyLand())){
            Player player = Bukkit.getPlayer(uuid);
            if (player != null){
                player.sendMessage(StringUtil.color("&8[&4&lWAR&8] &f" + msg));
            }
        }

    }
}
