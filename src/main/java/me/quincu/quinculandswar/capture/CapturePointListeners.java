package me.quincu.quinculandswar.capture;

import me.angeschossen.lands.api.events.land.block.LandBlockPlaceEvent;
import me.angeschossen.lands.api.flags.enums.FlagModule;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.framework.blockutil.BlockPosition;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.land.block.LandBlockType;
import me.angeschossen.lands.api.player.LandPlayer;
import me.angeschossen.lands.api.war.War;
import me.angeschossen.lands.api.war.enums.WarTeam;
import me.quincu.quinculandswar.QuincuLandsWar;
import me.quincu.quinculandswar.util.StringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CapturePointListeners implements Listener {

    private ArrayList<Location> recentlyPlaced;
    private CapturePointManager capturePointManager;

    public CapturePointListeners(CapturePointManager capturePointManager){
        this.capturePointManager = capturePointManager;
        this.recentlyPlaced = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event){
        if (event.isCancelled()) return;

        recentlyPlaced.add(event.getBlockPlaced().getLocation());

        Bukkit.getScheduler().runTaskLater(QuincuLandsWar.t, new Runnable() {
            @Override
            public void run() {
                recentlyPlaced.remove(event.getBlockPlaced().getLocation());
            }
        }, 160L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplodeOne(EntityExplodeEvent event){
        if (event.getEntityType() == EntityType.END_CRYSTAL){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplodeThree(BlockExplodeEvent event){
        if (event.getBlock().getType() == Material.RESPAWN_ANCHOR){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onExplodeTwo(EntityExplodeEvent event){

        for (Block b : event.blockList()){
            if (b.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                Location loc = b.getLocation();
                Location potentialCap = loc.clone().subtract(0, 1, 0);
                CapturePoint cp = capturePointManager.getFromBlock(potentialCap.getBlock());
                if (cp == null) continue;
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onExplode(EntityExplodeEvent event){
        CapturePoint c = null;
        for (Block b : event.blockList()){
            if (b.getType() == Material.RED_STAINED_GLASS_PANE) continue;
            if (b.getType() != Material.IRON_BLOCK && b.getType() != Material.BEACON) continue;
            CapturePoint cp = capturePointManager.getFromBlock(b);
            if (cp != null){
                c = cp;
                break;
            }
        }

        if (c == null) return;

        event.setCancelled(true);

        if (!c.isActive()) return;

        c.unbuild();
        if (c instanceof SpecialCapturePoint){
            SpecialCapturePoint specialCapturePoint = (SpecialCapturePoint) c;
            specialCapturePoint.broken();
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event){
        if (event.getClickedBlock() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getClickedBlock().getType().equals(Material.BEACON)) return;
        if (capturePointManager.getFromBlock(event.getClickedBlock()) != null) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMine(BlockBreakEvent event){
        CapturePoint c = capturePointManager.getFromBlock(event.getBlock());
        if (c == null) return;
        if (event.getBlock().getType() == Material.BEACON){
            c.unbuild();
            if (c instanceof SpecialCapturePoint){
                SpecialCapturePoint specialCapturePoint = (SpecialCapturePoint) c;
                specialCapturePoint.broken();
            }
            c.mine();
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (event.getBlock().getType() == Material.RED_STAINED_GLASS_PANE) return;
        if (!c.isActive()) return;
        int nearby = c.getNearbyInvaders();
        if (nearby > 0) {
            event.getPlayer().sendMessage(StringUtil.color("&cYou can't mine this cap rn! There are still &4" + c.getNearbyInvaders() + " &cnearby invaders!"));
            return;
        }
        c.unbuild();
        if (c instanceof SpecialCapturePoint){
            SpecialCapturePoint specialCapturePoint = (SpecialCapturePoint) c;
            specialCapturePoint.broken();
        }
        c.mine();
    }

    private static final Material[] em = new Material[]{
            Material.AIR,
            Material.WATER,
            Material.SHORT_GRASS,
            Material.TALL_GRASS,
            Material.SEAGRASS,
            Material.TALL_SEAGRASS,
            Material.FERN,
            Material.BEACON
    };

    public static final ArrayList<Material> emptyMaterials = new ArrayList<>(Arrays.asList(em));

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(LandBlockPlaceEvent event) {
        Player player = event.getLandPlayer().getPlayer();
        War war = event.getLand().getWar();
        if (war == null) return;
        if (war.isEndingSoon()) return;
        if (event.isCancelled()) return;
        if (!event.getLandBlock().getType().equals(LandBlockType.CAPTURE_FLAG)) return;

        event.setCancelled(true);
        event.getLandBlock().remove(new Runnable() {
            @Override
            public void run() {

            }
        }, 0);

        BlockPosition bp = event.getLandBlock().getCenter();
        Location location = new Location(bp.getWorld(), bp.getX(), bp.getY() - 1, bp.getZ());

        Land enemyLand = QuincuLandsWar.t.getLands().getLandByChunk(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ());
        if (enemyLand == null) return;

        WarTeam warTeam = WarTeam.DEFENDER;
        if (war.getDefender().getName().equalsIgnoreCase(enemyLand.getName())) {
            warTeam = WarTeam.ATTACKER;
        }

        Location[] locs = new Location[]{
                location.clone().add(1, 0, 1),
                location.clone().add(-1, 0, -1),
                location.clone().add(1, 0, -1),
                location.clone().add(-1, 0, 1),
//
        };
//
        int cX = location.getChunk().getX();
        int cZ = location.getChunk().getZ();
//
        for (Location l : locs) {
            Chunk c2 = l.getChunk();
            if (c2.getX() != cX || c2.getZ() != cZ) {
                player.sendMessage(ChatColor.RED + "The capture point must be placed where all of its iron blocks are in one chunk!");
                return;
            }
        }

        Land allyLand;
        if (warTeam == WarTeam.DEFENDER) {
            allyLand = QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        } else {
            allyLand = QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }

        long cd = capturePointManager.isOnCooldown(allyLand.getName());
        int seconds = ((int) cd / 1000) + 1;
        if (cd > 0) {
            player.sendMessage(StringUtil.color("&cYour team can place another cap in &4" + seconds + " &cseconds!"));
            return;
        }

        boolean hasWarFlag = false;
        for (me.angeschossen.lands.api.flags.type.RoleFlag rf : allyLand.getDefaultArea().getRole(player.getUniqueId()).getManagementFlags()) {
            if (rf.getBypassPermission().contains("war")) {
                hasWarFlag = true;
                break;
            }
        }

        if (!hasWarFlag) {
            player.sendMessage(StringUtil.color("&cYou need the flag &4WAR_MANAGE &cin order to place a cap!"));
            return;
        }

        int i = 0;
        for (CapturePoint cp : QuincuLandsWar.t.getCaptureBlockManager().getActiveCapturePoints()) {
            if (!(cp instanceof SpecialCapturePoint)) continue;
            SpecialCapturePoint scp = (SpecialCapturePoint) cp;
            if (!scp.isActive()) continue;

            Chunk capChunk = new Location(location.getWorld(), scp.getX(), scp.getY(), scp.getZ()).getChunk();
            if (capChunk.getX() == cX && capChunk.getZ() == cZ) {
                player.sendMessage(ChatColor.RED + "Your team already has a capture point in this chunk!");
                return;
            }

            if (scp.getWarTeam() == warTeam) i++;
        }

        if (i >= CapturePointManager.MAX_CAPS) {
            player.sendMessage(ChatColor.RED + "Your team already has " + CapturePointManager.MAX_CAPS + " caps placed!");
            return;
        }

        Location check1 = location.clone().add(0, -1, 0);
        Location check2 = location.clone().add(0, -2, 0);

        Location check3 = location.clone().add(0, -3, 0);

        List<String[]> list1 = QuincuLandsWar.t.coreProtectAPI.blockLookup(check1.getBlock(), 7200);
        List<String[]> list2 = QuincuLandsWar.t.coreProtectAPI.blockLookup(check2.getBlock(), 7200);

        long start = war.getStarted().getTime();

        boolean list1legal = true;
        for (String[] log : list1) {
            Long time = Long.parseLong(log[0]);
            time = time * 1000;
            if (time > start) {
                list1legal = false;
                break;
            }
        }

        boolean list2legal = true;
        for (String[] log : list2) {
            Long time = Long.parseLong(log[0]);
            time = time * 1000;
            if (time > start) {
                list2legal = false;
                break;
            }
        }

        for (Location l : recentlyPlaced){
            if (l.equals(check1)) {
                list1legal = false;
            }
            if (l.equals(check2)){
                list2legal = false;
            }
        }

        if (check2.getBlock().getType() == Material.AIR || check2.getBlock().getType() == Material.AIR) {
            list2legal = false;
        }

        if (!list1legal && !list2legal) {
            player.sendMessage(ChatColor.RED + "This would be an illegal sky cap!");
            return;
        }


        SpecialCapturePoint scp = QuincuLandsWar.t.getCaptureBlockManager().createCapturePoint(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld(),
                (int) (CapturePointManager.getHoldTimeWarsYAML() / 50),
                war,
                warTeam
        );
        capturePointManager.placeCap(allyLand.getName());

    }
    /*
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event){
        Block placed = event.getBlockPlaced();
        if (placed.getType() != Material.BEACON) return;
        ItemStack item = event.getItemInHand();
        ItemMeta im = item.getItemMeta();
        if (im == null) return;

        NamespacedKey landsKey = new NamespacedKey("lands", "type");
        String landType = im.getPersistentDataContainer().get(landsKey, PersistentDataType.STRING);
        if (landType == null) return;
        if (!landType.equalsIgnoreCase("CAPTURE_FLAG")) return;

        event.setCancelled(true);

        Location location = event.getBlockPlaced().getLocation();
        Chunk c = location.getChunk();
        Land land = QuincuLandsWar.t.getLands().getLandByChunk(c.getWorld(), c.getX(), c.getZ());

        if (land == null){
            event.getPlayer().sendMessage(ChatColor.RED + "Can't place a capture point in the wilderness!");
            return;
        }
//
        if (land.getTrustedPlayers().contains(event.getPlayer().getUniqueId())){
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a capture point in your own land!");
            return;
        }
//
        Location[] locs = new Location[]{
                location.clone().add(1, 0, 1),
                location.clone().add(-1, 0, -1),
                location.clone().add(1, 0, -1),
                location.clone().add(-1, 0, 1),
//
        };
//
        int cX = c.getX();
        int cZ = c.getZ();
//
        for (Location l : locs){
            Chunk c2 = l.getChunk();
            if (c2.getX() != cX || c2.getZ() != cZ){
                event.getPlayer().sendMessage(ChatColor.RED + "The capture point must be placed where all of its iron blocks are in one chunk!");
                return;
            }
        }
//
//
        if (land.getWar() == null){
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place a capture point if there isn't a war active!");
            return;
        }
//
        ArrayList<Location> allBlockInCap = new ArrayList<>();
        for (int i1 = -1; i1 < 2; i1++){
            for (int i2 = -1; i2 < 2; i2++){
//
                Location l = location.clone().add(i1, 0, i2);
                allBlockInCap.add(l);
//
            }
        }
//
        allBlockInCap.add(location.clone().add(0, 1, 0));
        allBlockInCap.add(location.clone().add(0,2,0));
//
        ArrayList<Material> emptyMats = new ArrayList<>();
        Collections.addAll(emptyMats, em);
//
        for (Location l : allBlockInCap){
            Block b = l.getBlock();
            QuincuLandsWar.t.getLogger().warning(b.getType().toString());
            if (!emptyMats.contains(b.getType())){
                event.getPlayer().sendMessage(ChatColor.RED + "The capture point doesn't have enough space to be placed!");
                return;
            }
        }
//
        War war = land.getWar();
        if (!war.getDefender().getTrustedPlayers().contains(event.getPlayer().getUniqueId()) && !war.getAttacker().getTrustedPlayers().contains(event.getPlayer().getUniqueId())){
            event.getPlayer().sendMessage(ChatColor.RED + "You need to be trusted to the lands to join the war!");
            return;
        }
//
        Land allyLand = null;
        if (war.getDefender().getTrustedPlayers().contains(event.getPlayer().getUniqueId())){
            allyLand = QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        }else {
            allyLand = QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }
//
        boolean canPlace = false;
        for (int i1 = -1; i1 < 2; i1 +=2){
            for (int i2 = -1; i2 < 2; i2 +=2){
                int c2X = cX + i1;
                int c2Z = cZ + i2;
                if (!land.hasChunk(location.getWorld(), c2X, c2Z)){
                    canPlace = true;
                    break;
                }
            }
        }
//
        if (!canPlace){
            event.getPlayer().sendMessage(ChatColor.RED + "The capture point must be placed in the outskirts of the enemy land.");
            return;
        }

        boolean hasWarFlag = false;
        for (me.angeschossen.lands.api.flags.type.RoleFlag rf : allyLand.getDefaultArea().getRole(event.getPlayer().getUniqueId()).getManagementFlags()){
            if (rf.getBypassPermission().contains("war")) {
                hasWarFlag = true;
                break;
            }
        }

        if (!hasWarFlag){
            event.getPlayer().sendMessage(StringUtil.color("&cYou need the flag &4WAR_MANAGE &cin order to place a cap!"));
            return;
        }

        WarTeam warTeam = war.getTeam(QuincuLandsWar.t.getLands().getLandPlayer(event.getPlayer().getUniqueId()));

        int i = 0;
        for (CapturePoint cp : QuincuLandsWar.t.getCaptureBlockManager().getActiveCapturePoints()){
            if (!(cp instanceof SpecialCapturePoint)) continue;
            SpecialCapturePoint scp = (SpecialCapturePoint) cp;
            if (!scp.isActive()) continue;

            Chunk capChunk = new Location(location.getWorld(), scp.getX(), scp.getY(), scp.getZ()).getChunk();
            if (capChunk.getX() == cX && capChunk.getZ() == cZ){
                event.getPlayer().sendMessage(ChatColor.RED + "Your team already has a capture point in this chunk!");
                return;
            }

            if (scp.getWarTeam() == warTeam) i++;
        }

        if (i >= 3) {
            event.getPlayer().sendMessage(ChatColor.RED + "Your team already has four caps placed!");
            return;
        }




        item.setAmount(item.getAmount() - 1);

    }
     */
}
