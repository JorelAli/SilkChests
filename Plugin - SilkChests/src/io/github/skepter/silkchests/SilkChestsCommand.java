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
		if(command.getName().equalsIgnoreCase("silkchests") && sender.hasPermission("silkchests.admin")) {
			if(args.length == 0) {
				/* AA center code goes here, list of commands (reload) (config - views current setup) */
				/* cmds to modify config ingame, make sure it updates config.yml file!! */
				sender.sendMessage(prefix + "");
			}
			if(args.length > 0) {
				switch(args[0].toLowerCase()) {
					case "reload":
						main.saveConfig();
						main.updateInternalConfig();
						sender.sendMessage(prefix + "SilkChests has been reloaded!");
						break;
					case "config":
						sender.sendMessage("Trapped chests enabled: " + String.valueOf(Main.trappedChests));
						sender.sendMessage("SilkChests inside Chests enabled: " + String.valueOf(Main.chestInChest));
						//allow changing if args[1] (if args.length >1)
						//on edit event, print new config
						main.updateExternalConfig();
						break;
				}
			}
			
		}
		return false;
	}

}
