package me.romix.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.romix.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Config {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final Logger logger;

    private Component prefix;
    private String staffPermission;
    private boolean broadcastToAll;
    private String punishCommand;
    private String messagePunished;
    private String messageAlert;

    private final List<ModEntry> punishMods = new ArrayList<>();
    private final List<ModEntry> alertMods  = new ArrayList<>();
    
    private List<ModEntry> allMods = Collections.emptyList();

	public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        load();
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    private void load() {
        punishMods.clear();
        alertMods.clear();

        try {
            var cfg = plugin.getConfig();

            prefix = MM.deserialize(cfg.getString("prefix", "<gray>[<#ff6060>DirtyDetect<gray>]<reset>"));
            staffPermission = cfg.getString("staff-permission", "dirtydetect.staff");
            broadcastToAll = cfg.getBoolean("broadcast-to-all", false);
            punishCommand = cfg.getString("punish.command", "ban %player% [DirtyDetect] Hacked client detected (%mod%).");
            messagePunished = cfg.getString("messages.punished", " <white>Player <yellow>%player%</yellow> was caught using <red>%mod%</red> and was punished.");
            messageAlert = cfg.getString("messages.alert", " <gold>[ALERT] <white>Player <yellow>%player%</yellow> may be using <gold>%mod%</gold>.");

            loadModSection(cfg.getConfigurationSection("punish.mods"), punishMods, true);
            loadModSection(cfg.getConfigurationSection("alert.mods"), alertMods, false);

            List<ModEntry> merged = new ArrayList<>(punishMods.size() + alertMods.size());
            merged.addAll(punishMods);
            merged.addAll(alertMods);
            allMods = Collections.unmodifiableList(merged);

            logger.info("Loaded " + punishMods.size() + " punish mods and " + alertMods.size() + " alert mods.");

        } catch (Exception e) {
            logger.severe("Failed to load config: " + e.getMessage());
        }
    }

    private void loadModSection(ConfigurationSection section, List<ModEntry> target, boolean punish) {
        if (section == null) return;
        
        for (String name : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(name);
            if (s == null) continue;
            String key = s.getString("key", "").trim();
            if (key.isEmpty()) {
                logger.warning("Mod '" + name + "' has no 'key' defined.");
                continue;
            }
            target.add(new ModEntry(name, key, punish));
        }
    }

    public void handleDetection(ModEntry mod, Player player) {
        if (!player.isOnline()) return;

        if (mod.punish()) {
            handlePunishment(mod, player);
        } else {
            handleAlert(mod, player);
        }
    }

    private void handlePunishment(ModEntry mod, Player player) {
        Component msg = prefix.append(MM.deserialize(
                messagePunished
                        .replace("%player%", player.getName())
                        .replace("%mod%", mod.name())));

        if (broadcastToAll) {
            Bukkit.broadcast(msg);
        } else {
            broadcastToStaff(msg);
        }

        String cmd = punishCommand
                .replace("%player%", player.getName())
                .replace("%mod%", mod.name());
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        
        try {
            Bukkit.dispatchCommand(console, cmd);
        } catch (Exception e) {
            logger.severe("Failed to dispatch punishment command for "
                    + player.getName() + ": " + e.getMessage());
        }
    }

    private void handleAlert(ModEntry mod, Player player) {
        Component msg = prefix.append(MM.deserialize(
                messageAlert
                        .replace("%player%", player.getName())
                        .replace("%mod%", mod.name())));
        broadcastToStaff(msg);
    }

    private void broadcastToStaff(Component message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(staffPermission)) {
                online.sendMessage(message);
            }
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public static Component format(String text) {
        return MM.deserialize(text);
    }

    public Component getPrefix() { return prefix;          }
    public String getStaffPermission() { return staffPermission; }
    public List<ModEntry> getAllMods() { return allMods; }

    public record ModEntry(String name, String key, boolean punish) {}
}