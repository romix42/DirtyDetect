package me.romix.command;

import me.romix.Main;
import me.romix.util.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Command implements CommandExecutor, TabCompleter {
	private final Main plugin;

	public Command(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label,
			@NotNull String[] args) {

		if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
			handleReload(sender);
			return true;
		}

		sendVersionInfo(sender);
		return true;
	}

	private void handleReload(CommandSender sender) {
		if (!sender.hasPermission("dirtydetect.reload")) {
			sender.sendMessage(Config.format("<#fd7474>❌ ᴇʀʀᴏʀ: <#ff6060>You don't have permission to reload Dirty Detect."));
			return;
		}

		try {
			plugin.getConfigUtil().reload();
			sender.sendMessage(Config.format("<#74fd7b>✔ ꜱᴜᴄᴄᴇꜱꜱ: <#92ff60>Config reloaded successfully."));
		} catch (Exception e) {
			sender.sendMessage(Config.format("<#fd7474>❌ ᴇʀʀᴏʀ: <#ff6060>Failed to reload the config."));
			plugin.getLogger().severe("Error while reloading the config: " + e.getMessage());
		}
	}

	private void sendVersionInfo(CommandSender sender) {
		Component prefix = plugin.getConfigUtil().getPrefix();

		sender.sendMessage(prefix.append(Config.format(
				" <white>Running <#F54927>Dirty Detect v" + Main.VERSION + "<reset>")));

		sender.sendMessage(prefix.append(Config.format(
				" <white>Made by <#27D3F5>" + Main.AUTHOR + "<reset>")));

		if (sender.hasPermission("dirtydetect.reload")) {
			if (plugin.isUpdateAvailable()) {
				sender.sendMessage(prefix.append(Config.format(
						" <#ff6060>Update available: <white>v" + plugin.getLatestVersion() + 
						" <reset><click:open_url:'" + Main.MODRINTH_URL + "'><#78B7FF><underlined>" + Main.MODRINTH_URL + "<reset>")));
			} else {
				sender.sendMessage(prefix.append(Config.format(" <#92ff60>You are running the latest version.")));
			}
		}
	}

	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
			@NotNull String alias, @NotNull String[] args) {
		if (args.length == 1 && sender.hasPermission("dirtydetect.reload")) {
			String partial = args[0].toLowerCase();
			if ("reload".startsWith(partial)) {
				List<String> list = new ArrayList<>();
				list.add("reload");
				return list;
			}
		}
		return Collections.emptyList();
	}
}