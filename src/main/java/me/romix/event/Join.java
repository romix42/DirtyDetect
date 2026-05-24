package me.romix.event;

import me.romix.Main;
import me.romix.util.Config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Join implements Listener {

    private static final int MODS_PER_SIGN = 4;
    private static final long JOIN_DELAY_TICKS = 40L;
    private static final long SIGN_OPEN_DELAY_TICKS = 3L;
    private static final long BATCH_DELAY_TICKS = 10L;

    private final Map<UUID, ScanRequest> pending = new ConcurrentHashMap<>();

    private final Config config;
    private final Logger     logger;
    
    public Join(JavaPlugin plugin, Config configUtil) {
        this.config = configUtil;
        this.logger = plugin.getLogger();
        org.bukkit.Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("dirtydetect.bypass")) return;

        if (pending.containsKey(player.getUniqueId())) return;

        List<Config.ModEntry> allMods = config.getAllMods();
        if (allMods.isEmpty()) return;

        Main.getWrappedScheduler().runTaskLaterAtEntity(player, () -> {
            if (!player.isOnline()) return;

            Queue<List<Config.ModEntry>> batches = new LinkedList<>();
            for (int i = 0; i < allMods.size(); i += MODS_PER_SIGN) {
                batches.add(new ArrayList<>(
                        allMods.subList(i, Math.min(i + MODS_PER_SIGN, allMods.size()))));
            }

            startBatch(player, batches);
        }, JOIN_DELAY_TICKS);
    }

    private void startBatch(Player player, Queue<List<Config.ModEntry>> remaining) {
        List<Config.ModEntry> batch = remaining.poll();
        if (batch == null || !player.isOnline()) return;

        Location signLoc = player.getLocation().clone();
        signLoc.setY(Math.max(
                signLoc.getWorld().getMinHeight() + 1,
                signLoc.getBlockY() - 3));

        Main.getWrappedScheduler().runTaskAtLocation(signLoc, () -> {
            Block     block    = signLoc.getBlock();
            BlockData oldData  = block.getBlockData().clone();
            BlockState oldState = block.getState();

            block.setType(Material.DARK_OAK_SIGN, false);

            BlockState newState = block.getState();
            if (!(newState instanceof Sign sign)) {
                logger.warning("[DirtyDetect] Could not place sign at "
                        + formatLoc(signLoc) + " for " + player.getName()
                        + " – block type is " + block.getType());
                
                restoreBlock(oldState, oldData, signLoc);
                return;
            }

            var back = sign.getSide(Side.BACK);
            for (int i = 0; i < batch.size(); i++) {
                back.line(i, Component.translatable(batch.get(i).key()));
            }
            sign.update(false, false);

            pending.put(player.getUniqueId(),
                    new ScanRequest(batch, remaining, oldData, oldState, signLoc));

            Main.getWrappedScheduler().runTaskLaterAtEntity(player, () -> {
                if (!player.isOnline()) {
                    cleanUp(player.getUniqueId());
                    return;
                }
                
                BlockState freshState = signLoc.getBlock().getState();
                if (!(freshState instanceof Sign freshSign)) {
                    logger.warning("[DirtyDetect] Sign at " + formatLoc(signLoc)
                            + " disappeared before opening for " + player.getName());
                    cleanUp(player.getUniqueId());
                    return;
                }
                try {
                    player.openSign(freshSign, Side.BACK);
                    player.closeInventory();
                } catch (Exception e) {
                    logger.warning("[DirtyDetect] openSign failed for "
                            + player.getName() + ": " + e.getMessage());
                    cleanUp(player.getUniqueId());
                }
            }, SIGN_OPEN_DELAY_TICKS);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onSignChange(SignChangeEvent event) {
        Player      player  = event.getPlayer();
        ScanRequest request = pending.remove(player.getUniqueId());
        if (request == null) return;

        event.setCancelled(true);

        List<Config.ModEntry> batch = request.batch();

        for (int i = 0; i < batch.size(); i++) {
            Config.ModEntry mod  = batch.get(i);
            Component           line = event.line(i);
            if (line == null) continue;

            String submitted = PlainTextComponentSerializer.plainText().serialize(line);

            if (submitted.isEmpty() || submitted.equals(mod.key())) continue;

            logger.warning("[DirtyDetect] " + player.getName()
                    + " | mod=" + mod.name()
                    + " | expected=\"" + mod.key() + "\""
                    + " | got=\"" + submitted + "\""
                    + " | punish=" + mod.punish());

            final Config.ModEntry detectedMod = mod;
            Main.getWrappedScheduler().runTaskAtEntity(player, () -> {
                if (player.isOnline()) {
                    config.handleDetection(detectedMod, player);
                }
            });
        }

        restoreBlock(request.oldState(), request.oldBlockData(), request.signLoc());

        if (!request.remaining().isEmpty()) {
            Main.getWrappedScheduler().runTaskLaterAtEntity(player, () -> {
                if (player.isOnline()) {
                    startBatch(player, request.remaining());
                }
            }, BATCH_DELAY_TICKS);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        cleanUp(event.getPlayer().getUniqueId());
    }

    private void cleanUp(UUID uuid) {
        ScanRequest request = pending.remove(uuid);
        if (request == null) return;
        Main.getWrappedScheduler().runTaskAtLocation(request.signLoc(), () ->
                restoreBlock(request.oldState(), request.oldBlockData(), request.signLoc()));
    }

    private void restoreBlock(BlockState oldState, BlockData oldData, Location loc) {
        try {
            if (oldState != null) {
                oldState.update(true, false);
            } else {
                loc.getBlock().setBlockData(oldData, false);
            }
        } catch (Exception e) {
            logger.warning("[DirtyDetect] Failed to restore block at "
                    + formatLoc(loc) + ": " + e.getMessage());
        }
    }

    public void cleanup() {
    		for (UUID uuid : new ArrayList<>(pending.keySet())) {
            ScanRequest req = pending.remove(uuid);
            if (req == null) continue;
            try {
                restoreBlock(req.oldState(), req.oldBlockData(), req.signLoc());
            }
            catch (Exception ignored) {}
        }
    }

    private static String formatLoc(Location loc) {
        return loc.getWorld().getName()
                + " " + loc.getBlockX()
                + " " + loc.getBlockY()
                + " " + loc.getBlockZ();
    }

    private record ScanRequest(
            List<Config.ModEntry> batch,
            Queue<List<Config.ModEntry>> remaining,
            BlockData oldBlockData,
            BlockState oldState,
            Location signLoc
    ) {}
}