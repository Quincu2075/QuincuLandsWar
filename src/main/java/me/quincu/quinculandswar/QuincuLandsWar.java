package me.quincu.quinculandswar;

import me.angeschossen.lands.api.LandsIntegration;
import me.quincu.quinculandswar.capture.CapturePoint;
import me.quincu.quinculandswar.capture.CapturePointManager;
import me.quincu.quinculandswar.death.DeathListener;
import me.quincu.quinculandswar.scheduler.FoliaScheduler;
import me.quincu.quinculandswar.scheduler.PaperScheduler;
import me.quincu.quinculandswar.scheduler.ServerScheduler;
import me.quincu.quinculandswar.util.PlayerUtils;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class QuincuLandsWar extends JavaPlugin implements Listener {

    public static QuincuLandsWar t;
    public static YamlConfiguration warsYaml;
    private LandsIntegration l;
    public CoreProtectAPI coreProtectAPI;
    private CapturePointManager captureBlockManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        t = this;
        this.coreProtectAPI = CoreProtect.getInstance().getAPI();
        l = LandsIntegration.of(this);

        File file = new File("plugins/Lands/wars.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        warsYaml = yamlConfiguration;

        try {
            ServerScheduler.set(new PaperScheduler());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        this.captureBlockManager = new CapturePointManager();
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerUtils(), this);

        for (CapturePoint cp : captureBlockManager.getActiveCapturePoints()){
            cp.deleteReboot();
        }
    }

    public CapturePointManager getCaptureBlockManager() {
        return captureBlockManager;
    }

    public LandsIntegration getLands(){
        return l;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
