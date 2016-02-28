package io.github.skepter.silkchests;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SilkChestsCommand implements CommandExecutor {

	String prefix = ChatColor.GOLD + "[" + ChatColor.YELLOW + "SilkChests" + ChatColor.GOLD + "] " + ChatColor.WHITE;
	Main main;

	public SilkChestsCommand(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("silkchests") && sender.hasPermission("silkchests.admin")) {
			if (args.length == 0) {
				sender.sendMessage(Utils.center("-- " + prefix + main.getDescription().getVersion() +  " --"));
				sender.sendMessage("Available commands:");
				sender.sendMessage(ChatColor.YELLOW + "/silkchest reload" + ChatColor.WHITE
						+ " - reloads the SilkChest config");
				sender.sendMessage(ChatColor.YELLOW + "/silkchest config" + ChatColor.WHITE
						+ " - shows what is currently enabled/disabled");
				sender.sendMessage(ChatColor.YELLOW + "/silkchest configupdate [config key] [config value]" + ChatColor.WHITE
						+ " - edits the config ingame");
			}
			if (args.length > 0) {
				switch (args[0].toLowerCase()) {
					case "reload":
						main.saveConfig();
						main.updateInternalConfig();
						sender.sendMessage(prefix + "SilkChests has been reloaded!");
						break;
					case "config":
						sender.sendMessage("Trapped chests enabled: " + getBoolean(Main.trappedChests));
						sender.sendMessage("SilkChests inside Chests enabled: " + getBoolean(Main.chestInChest));
						break;
					case "configupdate":
						sender.sendMessage(prefix + "Current keys: canStoreChestInChest, useTrappedChests");
						if (args.length == 3) {
							switch (args[1].toLowerCase()) {
								case "canstorechestinchest":
									Main.chestInChest = Boolean.parseBoolean(args[2]);
									break;
								case "usetrappedchests":
									Main.trappedChests = Boolean.parseBoolean(args[2]);
									break;
								default:
									sender.sendMessage(prefix + "Invalid syntax, use /silkchests to see the format");
									return true;
							}
							main.updateExternalConfig();
							sender.sendMessage("Trapped chests enabled: " + getBoolean(Main.trappedChests));
							sender.sendMessage("SilkChests inside Chests enabled: " + getBoolean(Main.chestInChest));
						} else {
							sender.sendMessage(prefix + "Invalid syntax, use /silkchests to see the format");
						}
						break;
				}
			}
			return true;
		}
		return false;
	}
	
	private String getBoolean(boolean value) {
		return value ? ChatColor.GREEN + String.valueOf(value) : ChatColor.RED + String.valueOf(value);
	}

}
