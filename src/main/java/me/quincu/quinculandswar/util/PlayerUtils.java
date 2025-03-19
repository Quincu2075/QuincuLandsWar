package me.quincu.quinculandswar.util;

import me.angeschossen.lands.api.events.war.WarEndEvent;
import me.angeschossen.lands.api.events.war.WarStartEvent;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.relations.Relation;
import me.angeschossen.lands.api.war.War;
import me.quincu.quinculandswar.QuincuLandsWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerUtils implements Listener {

    //No getters in lands API for wars, so we have to manually get them with events
    private static ArrayList<War> activeWars;

    public PlayerUtils(){
        activeWars = new ArrayList<>();

        //Load wars from restart
        for (Land l : QuincuLandsWar.t.getLands().getLands()){
            if (l.getWar() == null) continue;
            War war = l.getWar();
            if (alreadyContains(war)) continue;
            activeWars.add(war);
        }
    }

    private boolean alreadyContains(War war){
        for (War war1 : activeWars){
            if (war1.getDefender().getName().equalsIgnoreCase(war.getDefender().getName()) && war1.getAttacker().getName().equalsIgnoreCase(war.getAttacker().getName())) return true;
        }
        return false;
    }

    @EventHandler
    public void onWarStart(WarStartEvent event){
        activeWars.add(event.getWar());
    }

    @EventHandler
    public void onWarEnd(WarEndEvent event){
        activeWars.remove(event.getWar());
    }

    public static ArrayList<War> getActiveWars(){
        //Check if war is ended via admin commands (Doesn't called war end event sometimes)
        activeWars.removeIf(War::isEndingSoon);
        return activeWars;
    }

    public static List<UUID> getPlayerWarList(Land land){
        List<UUID> list = new ArrayList<>(land.getTrustedPlayers());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (land.getRelation(player.getUniqueId()) == Relation.ALLY) {
                list.add(player.getUniqueId());
            }
        }
        if (land.getNation() != null){
            for (Land nLand : land.getNation().getLands()){
                for (UUID uuid : nLand.getTrustedPlayers()){
                    if (!list.contains(uuid)){
                        list.add(uuid);
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (nLand.getRelation(player.getUniqueId()) == Relation.ALLY) {
                        if (!list.contains(player.getUniqueId())){
                            list.add(player.getUniqueId());
                        }
                    }
                }
            }
        }
        return list;
    }

    public static ArrayList<War> getPlayerWars(Player player){
        ArrayList<War> playerWars = new ArrayList<>();
        for (War war : activeWars){
            Land defense = QuincuLandsWar.t.getLands().getLandByULID(war.getDefender().getULID());
            if (defense.getTrustedPlayers().contains(player.getUniqueId()) || defense.getRelation(player.getUniqueId()) == Relation.ALLY) {
                addIfDoesntContain(playerWars, war);
                continue;
            }
            if (defense.getNation() != null){
                for (Land l : defense.getNation().getLands()){
                    boolean found = false;
                    if (l.getTrustedPlayers().contains(player.getUniqueId())){
                        addIfDoesntContain(playerWars, war);
                        found = true;
                    }
                    if (found) continue;
                }
                if (defense.getRelation(player.getUniqueId()) == Relation.ALLY){
                    addIfDoesntContain(playerWars, war);
                    continue;
                }
            }
            Land offense = QuincuLandsWar.t.getLands().getLandByULID(war.getAttacker().getULID());
            if (offense.getTrustedPlayers().contains(player.getUniqueId()) || offense.getRelation(player.getUniqueId()) == Relation.ALLY) {
                addIfDoesntContain(playerWars, war);
            }
            if (offense.getNation() != null){
                for (Land l : offense.getNation().getLands()){
                    boolean found = false;
                    if (l.getTrustedPlayers().contains(player.getUniqueId())){
                        addIfDoesntContain(playerWars, war);
                        found = true;
                    }
                    if (found) continue;
                }
                if (offense.getRelation(player.getUniqueId()) == Relation.ALLY){
                    addIfDoesntContain(playerWars, war);
                    continue;
                }
            }
        }
        return playerWars;
    }

    private static void addIfDoesntContain(List<War> list, War specific){
        if (!list.contains(specific)) list.add(specific);
    }

}
