package me.romix;

import me.romix.command.Command;
import me.romix.event.Join;
import me.romix.util.Config;
import me.romix.util.UpdateChecker;

import me.nahu.scheduler.wrapper.FoliaWrappedJavaPlugin;
import me.nahu.scheduler.wrapper.WrappedScheduler;
import org.bukkit.command.PluginCommand;

public final class Main extends FoliaWrappedJavaPlugin {

    public static final String VERSION      = "1.0";
    public static final String AUTHOR       = "romix_42";
    public static final String MODRINTH_ID  = "idek";
    public static final String MODRINTH_URL = "https://modrinth.com/plugin/dirtydetect";

    private static Main instance;

    private Config configUtil;
    private Join joinEvent;

    private String latestVersion = null;
    private boolean updateAvailable = false;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Failed to create data folder.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();

        configUtil = new Config(this);
        joinEvent  = new Join(this, configUtil);

        PluginCommand cmd = getCommand("dirtydetect");
        if (cmd != null) {
            Command handler = new Command(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        } else {
            getLogger().severe("Failed to load commands.");
        }

        getServer().getScheduler().runTaskAsynchronously(this, new UpdateChecker(this));

        getLogger().info("DirtyDetect v" + VERSION + " by " + AUTHOR + " enabled.");
    }

    @Override
    public void onDisable() {
        if (joinEvent != null) {
            joinEvent.cleanup();
        }
        getLogger().info("DirtyDetect disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    public static WrappedScheduler getWrappedScheduler() {
        return instance.getScheduler();
    }

    public Config getConfigUtil() { return configUtil; }
    public Join getJoinEvent() { return joinEvent;  }

    public String getLatestVersion() { return latestVersion;   }
    public boolean isUpdateAvailable() { return updateAvailable; }

    public void setLatestVersion(String version) {
        this.latestVersion = version;
        this.updateAvailable = version != null && !VERSION.equals(version);
    }
}