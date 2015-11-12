package io.github.skepter.silkchests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.yi.acru.bukkit.Lockette.Lockette;

import com.sk89q.worldguard.bukkit.WGBukkit;

public class Main extends JavaPlugin implements Listener {

	private boolean hasLockette = false;
	private boolean hasWorldGuard = false;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		if (Bukkit.getPluginManager().getPlugin("Lockette") != null)
			hasLockette = true;
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
			hasWorldGuard = true;
	}

	public void onDisable() {
		saveConfig();
	}

	/* Checks if the player is allowed to place the block */
	public boolean placingChecks(BlockPlaceEvent event) {
		ItemStack is = event.getItemInHand();
		if(is != null) {
			if (hasWorldGuard) {
				return (is.getType().equals(Material.CHEST) && is.getItemMeta().getLore().contains("SilkChest") && WGBukkit.getPlugin().canBuild(event.getPlayer(), event.getBlock()));
			} else {
				return (is.getType().equals(Material.CHEST) && is.getItemMeta().getLore().contains("SilkChest"));
			}
		}
		return false;
	}

	/* Checks if the player is allowed to break the block */
	public boolean breakingChecks(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (hasWorldGuard && hasLockette) {
			return (player.hasPermission("silkchest.use") && block.getType().equals(Material.CHEST) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest
					&& WGBukkit.getPlugin().canBuild(player, block) && Lockette.isOwner(block, player));
		} else if (hasWorldGuard) {
			return (player.hasPermission("silkchest.use") && block.getType().equals(Material.CHEST) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest && WGBukkit.getPlugin().canBuild(player,
					block));
		} else if (hasLockette) {
			return (player.hasPermission("silkchest.use") && block.getType().equals(Material.CHEST) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest && Lockette.isOwner(block, player));
		} else {
			return (player.hasPermission("silkchest.use") && block.getType().equals(Material.CHEST) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest);
		}

	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		if (placingChecks(event)) {

			/* Get deserialized items and put it into the chest */
			Collection<ItemStack> items = deserialize(Utils.getNBT(event.getItemInHand()));
			if ((event.getBlock().getState() instanceof Chest)) {
				Chest chest = (Chest) event.getBlock().getState();
				for (ItemStack is1 : items) {
					if (is1 != null) {
						chest.getInventory().addItem(new ItemStack[] { is1 });
					}
				}
			}
		}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		if (breakingChecks(event)) {

			/* Serialize chest contents */
			Player player = event.getPlayer();
			Chest chest = (Chest) event.getBlock().getState();
			event.setCancelled(true);
			
			/* Prevents SilkChests being stored in SilkChests */
			if(!getConfig().getBoolean("canStoreChestInChest")) {
				
				/* Removes the SilkChests from the chest and drops them */
				for(int i = 0; i < chest.getInventory().getContents().length; i++) {
					ItemStack is = chest.getInventory().getItem(i);
					if(is != null) {
						if(is.getType().equals(Material.CHEST)) {
							if(is.getItemMeta().getLore().get(0).equals("SilkChest")) {
								player.getWorld().dropItem(event.getBlock().getLocation(), is);
								chest.getInventory().remove(is);
							}
						}
					}
				}
			}
			
			String serializedString = serialize(Arrays.asList(chest.getInventory().getContents()));

			/* Create the chest item */
			ItemStack is = new ItemStack(Material.CHEST);
			ItemMeta meta = is.getItemMeta();
			meta.setLore(Arrays.asList(new String[] { "SilkChest" }));
			is.setItemMeta(meta);

			/*
			 * Add the contents to the chest, drop the item and remove the chest
			 * block
			 */
			ItemStack newItemStack = Utils.setNBT(is, serializedString);
			player.getWorld().dropItem(event.getBlock().getLocation(), newItemStack);
			chest.getInventory().clear();
			event.getBlock().setType(Material.AIR);
		}
	}

	/* Turns item stacks into a string */
	public String serialize(List<ItemStack> items) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Items", items);
		return Base64Coder.encodeString(config.saveToString());
	}

	/* Converts the string back into itemstacks */
	@SuppressWarnings("unchecked")
	public List<ItemStack> deserialize(String s) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(Base64Coder.decodeString(s));
			return (List<ItemStack>) config.get("Items");
		} catch (InvalidConfigurationException e) {
		}
		return null;
	}
}
