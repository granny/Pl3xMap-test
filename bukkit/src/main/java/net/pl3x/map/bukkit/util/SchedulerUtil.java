package net.pl3x.map.bukkit.util;

import net.pl3x.map.core.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class SchedulerUtil {
    private static boolean IS_FOLIA = false;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            IS_FOLIA = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static void runTaskTimer(Plugin plugin, Scheduler scheduler, BukkitScheduler bukkitScheduler) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, timerTask ->
                    scheduler.tick(), 20, 1);
        } else {
            bukkitScheduler.runTaskTimer(plugin, () ->
                    scheduler.tick(), 20, 1);
        }
    }
}
