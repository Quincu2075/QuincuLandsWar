package me.quincu.quinculandswar.death;

import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.LandPlayer;
import me.angeschossen.lands.api.war.War;
import me.angeschossen.lands.api.war.enums.WarTeam;
import me.quincu.quinculandswar.QuincuLandsWar;
import me.quincu.quinculandswar.capture.CapturePointManager;
import me.quincu.quinculandswar.util.PlayerUtils;
import me.quincu.quinculandswar.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.UUID;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event){

        Player player = event.getPlayer();

        for (War war : PlayerUtils.getPlayerWars(player)){
            Land playerAlly = null;
            WarTeam playerTeam = null;
            Land playerEnemy = null;
            if (PlayerUtils.getPlayerWarList(QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName())).contains(player.getUniqueId())){
                playerAlly = QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
                playerEnemy = QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
                playerTeam = WarTeam.DEFENDER;
            }else {
                playerAlly = QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
                playerEnemy = QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
                playerTeam = WarTeam.ATTACKER;
            }

            ArrayList<UUID> allyMsgsd = new ArrayList<>();
            for (UUID ally : PlayerUtils.getPlayerWarList(playerAlly)){
                if (allyMsgsd.contains(ally)) continue;
                allyMsgsd.add(ally);
                Player msgPlayer = Bukkit.getPlayer(ally);
                if (msgPlayer != null){
                    if (player.getKiller() == null) {
                        msgPlayer.sendMessage(StringUtil.color("&8[&4&lWAR&8] &fOur ally &a" + player.getName() + " &fhas died!"));
                    }else {
                        msgPlayer.sendMessage(StringUtil.color("&8[&4&lWAR&8] &fOur ally &a" + player.getName() + " &fhas been killed by &a" + player.getKiller().getName() + "&f!"));
                    }
                }
            }

            ArrayList<UUID> enemyMsgd = new ArrayList<>();
            for (UUID enemy : PlayerUtils.getPlayerWarList(playerEnemy)){
                if (enemyMsgd.contains(enemy)) continue;
                enemyMsgd.add(enemy);
                Player msgPlayer = Bukkit.getPlayer(enemy);
                if (msgPlayer != null){
                    if (player.getKiller() == null) {
                        msgPlayer.sendMessage(StringUtil.color("&8[&4&lWAR&8] &fOur enemy &a" + player.getName() + " &fhas died! &a(+" + CapturePointManager.KILL_POINTS  + " pts)"));
                    }else {
                        msgPlayer.sendMessage(StringUtil.color("&8[&4&lWAR&8] &fOur enemy &a" + player.getName() + " &fhas been killed by &a" + player.getKiller().getName() + "&f! &a(+" + CapturePointManager.KILL_POINTS  + " pts)"));
                    }
                }
            }

            if (playerTeam == WarTeam.ATTACKER){
                war.getDefenderStats().modifyPoints(CapturePointManager.KILL_POINTS);
            }else {
                war.getAttackerStats().modifyPoints(CapturePointManager.KILL_POINTS);
            }

        }


    }

    private Land getAlliedLand(Player player, War war){
        LandPlayer lp = QuincuLandsWar.t.getLands().getLandPlayer(player.getUniqueId());
        WarTeam wt = war.getTeam(lp);
        if (wt.equals(WarTeam.ATTACKER)){
            return QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }else {
            return QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        }
    }

    private Land getEnemyLand(Player player, War war){
        LandPlayer lp = QuincuLandsWar.t.getLands().getLandPlayer(player.getUniqueId());
        WarTeam wt = war.getTeam(lp);
        if (wt.equals(WarTeam.ATTACKER)){
            return QuincuLandsWar.t.getLands().getLandByName(war.getDefender().getName());
        }else {
            return QuincuLandsWar.t.getLands().getLandByName(war.getAttacker().getName());
        }
    }

}
