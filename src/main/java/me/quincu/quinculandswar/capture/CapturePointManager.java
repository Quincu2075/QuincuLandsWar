package me.quincu.quinculandswar.capture;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.war.War;
import me.angeschossen.lands.api.war.enums.WarTeam;
import me.quincu.quinculandswar.QuincuLandsWar;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;

public class CapturePointManager {

    private ArrayList<CapturePoint> activeCapturePoints;
    private HashMap<String, Long> capsPlaced;
    private static final long msCapCD = getCdFromWarsYAML();
    public static final int MAX_CAPS = QuincuLandsWar.warsYaml.getInt("capture.max");
    public static final int CAPTURE_POINTS = QuincuLandsWar.warsYaml.getInt("points.capture-block.capture");
    public static final int MINE_POINTS = QuincuLandsWar.warsYaml.getInt("points.capture-block.break");
    public static final int KILL_POINTS = 1;


    public CapturePointManager(){
        this.activeCapturePoints = new ArrayList<>();
        this.capsPlaced = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(new CapturePointListeners(this), QuincuLandsWar.t);
    }

    private static long getCdFromWarsYAML(){
        String value = QuincuLandsWar.warsYaml.getString("capture.place-cooldown_time");
        if (value.contains("s")){
            value = value.replaceAll("s", "");
            return Long.parseLong(value) * 1000L;
        }

        value = value.replaceAll("m", "");
        return Long.parseLong(value) * 1000L * 60L;
    }

    public static long getHoldTimeWarsYAML(){
        String value = QuincuLandsWar.warsYaml.getString("capture.hold_time");
        if (value.contains("s")){
            value = value.replaceAll("s", "");
            return Long.parseLong(value) * 1000L;
        }

        value = value.replaceAll("m", "");
        return Long.parseLong(value) * 1000L * 60L;
    }

    public ArrayList<CapturePoint> getActiveCapturePoints() {
        return activeCapturePoints;
    }

    public void remove(CapturePoint capturePoint){
        this.activeCapturePoints.remove(capturePoint);
    }

    // returns the MS left on cooldown. 0 if not on cd
    public long isOnCooldown(String land){
        if (!capsPlaced.containsKey(land)) return 0L;
        long lastPlacement = capsPlaced.get(land);
        long nextPlacement = lastPlacement + msCapCD;
        long currentMs = System.currentTimeMillis();

        if (currentMs >= nextPlacement) return 0L;

        return nextPlacement - currentMs;
    }

    public void placeCap(String land){
        this.capsPlaced.remove(land);
        this.capsPlaced.put(land, System.currentTimeMillis());
    }

    public void add(CapturePoint capturePoint){
        this.activeCapturePoints.add(capturePoint);
    }

    public CapturePoint getFromBlock(Block b){
        for (CapturePoint c : activeCapturePoints){
            if (c.contains(b.getLocation())) return c;
        }
        return null;
    }

    public CapturePoint createCapturePoint(int x, int y, int z, World world, int totalTicks){
        CapturePoint capturePoint = new CapturePoint(x, y, z, world, totalTicks);
        this.activeCapturePoints.add(capturePoint);
        return capturePoint;
    }


    public SpecialCapturePoint createCapturePoint(int x, int y, int z, World world, int totalTicks, War war, WarTeam warTeam){
        SpecialCapturePoint capturePoint = new SpecialCapturePoint(x, y, z, world, totalTicks, war, warTeam);
        this.activeCapturePoints.add(capturePoint);
        return capturePoint;
    }
}
