package io.github.skepter.silkchests;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SilkChestsCommand implements CommandExecutor {

	String prefix = ChatColor.GOLD + "[" + ChatColor.YELLOW + "SilkChests" + ChatColor.GOLD + "] " + ChatColor.YELLOW;
	
	public SilkChestsCommand(Main main) {
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
				switch(args[0]) {
					
				}
			}
			
		}
		return false;
	}

}
