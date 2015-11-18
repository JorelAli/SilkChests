package io.github.skepter.silkchests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
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

	/* Checks if the block is a chest or a trapped chest */
	public boolean chestCheck(Material material) {
		if (getConfig().getBoolean("useTrappedChests")) {
			return (material.equals(Material.CHEST) || material.equals(Material.TRAPPED_CHEST));
		} else {
			return (material.equals(Material.CHEST));
		}
	}

	/* Checks if the player is allowed to place the block */
	public boolean placingChecks(BlockPlaceEvent event) {
		ItemStack is = event.getItemInHand();
		if (is != null) {
			if (chestCheck(is.getType()) && is.getItemMeta().hasLore()) {
				if (is.getItemMeta().getLore().get(0).contains("SilkChest")) {
					if (hasWorldGuard) {
						return (WGBukkit.getPlugin().canBuild(event.getPlayer(), event.getBlock()));
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	/* Checks if the player is allowed to break the block */
	public boolean breakingChecks(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (hasWorldGuard && hasLockette) {
			return (player.hasPermission("silkchest.use") && chestCheck(block.getType()) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest && WGBukkit.getPlugin().canBuild(player, block) && Lockette
					.isOwner(block, player));
		} else if (hasWorldGuard) {
			return (player.hasPermission("silkchest.use") && chestCheck(block.getType()) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest && WGBukkit.getPlugin().canBuild(player, block));
		} else if (hasLockette) {
			return (player.hasPermission("silkchest.use") && chestCheck(block.getType()) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest && Lockette.isOwner(block, player));
		} else {
			return (player.hasPermission("silkchest.use") && chestCheck(block.getType()) && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && block.getState() instanceof Chest);
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

	public boolean isDoubleChest(Block block) {
		if(block.getState() instanceof Chest) {
			Chest c = (Chest) block.getState();
			return (c.getInventory().getHolder() instanceof DoubleChest);
		}
		return false;
	}
	
	@EventHandler
	public void blockBreakDoublechest(BlockBreakEvent event) {
		// Handle doublechests.
		/*
		 * I've decided to handle doublechests separately Because of the way
		 * they mess things up :P
		 */

		if (breakingChecks(event) && isDoubleChest(event.getBlock())) {
			System.out.println("Doublechest");
			Chest c = (Chest) event.getBlock().getState();
			DoubleChest dc = (DoubleChest) c.getInventory().getHolder();
			
			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();
			Chest chest = null;
			
			if(event.getBlock().equals(left.getBlock())) {
				chest = left;
			} else if (event.getBlock().equals(right.getBlock())){
				chest = right;
			} else {
				throw new NullPointerException();
			}

			/* Serialize chest contents */
			Player player = event.getPlayer();
			event.setCancelled(true);

			/* Prevents SilkChests being stored in SilkChests */
			if (!getConfig().getBoolean("canStoreChestInChest")) {

				/* Removes the SilkChests from the chest and drops them */
				for (int i = 0; i < chest.getInventory().getContents().length; i++) {
					ItemStack is = chest.getInventory().getItem(i);
					if (is != null) {
						if (chestCheck(is.getType()) && is.hasItemMeta()) {
							if (is.getItemMeta().hasLore()) {
								if (!is.getItemMeta().getLore().isEmpty()) {
									if (is.getItemMeta().getLore().get(0).equals("SilkChest")) {
										player.getWorld().dropItem(event.getBlock().getLocation(), is);
										chest.getInventory().remove(is);
									}
								}
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

	@EventHandler
	public void blockBreakChest(BlockBreakEvent event) {
		if (breakingChecks(event) && !(isDoubleChest(event.getBlock()))) {
			System.out.println("Chest");
			
			/* Serialize chest contents */
			Player player = event.getPlayer();
			Chest chest = (Chest) event.getBlock().getState();
			event.setCancelled(true);

			/* Prevents SilkChests being stored in SilkChests */
			if (!getConfig().getBoolean("canStoreChestInChest")) {

				/* Removes the SilkChests from the chest and drops them */
				for (int i = 0; i < chest.getInventory().getContents().length; i++) {
					ItemStack is = chest.getInventory().getItem(i);
					if (is != null) {
						if (chestCheck(is.getType()) && is.hasItemMeta()) {
							if (is.getItemMeta().hasLore()) {
								if (!is.getItemMeta().getLore().isEmpty()) {
									if (is.getItemMeta().getLore().get(0).equals("SilkChest")) {
										player.getWorld().dropItem(event.getBlock().getLocation(), is);
										chest.getInventory().remove(is);
									}
								}
							}
						}
					}
				}
			}

			String serializedString = serialize(Arrays.asList(chest.getInventory().getContents()));

			/* Create the chest item */
			ItemStack is = new ItemStack(Material.CHEST);
			if (getConfig().getBoolean("useTrappedChests")) {
				if (event.getBlock().getType().equals(Material.TRAPPED_CHEST)) {
					is = new ItemStack(Material.TRAPPED_CHEST);
				}
			}
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
