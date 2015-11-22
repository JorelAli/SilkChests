package io.github.skepter.silkchests;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.yi.acru.bukkit.Lockette.Lockette;

import com.sk89q.worldguard.bukkit.WGBukkit;

public class Main extends JavaPlugin implements Listener {

	/*
	 * Main plans for version 1.5: * Allow the option to remove the "SilkChest"
	 * lore from an item * Look into adding silkfurnaces and other silk items
	 * which can be toggled * Add a toggle for enabling/disabling silkchests in
	 * the config * A reload command? * A command to state what's enabled and
	 * what's disabled
	 * 
	 * * CLEAN UP THIS CODE! IT'S IN SUCH A STATE XD
	 */
	private boolean hasLockette = false;
	private boolean hasWorldGuard = false;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		if (Bukkit.getPluginManager().getPlugin("Lockette") != null)
			hasLockette = true;
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
			hasWorldGuard = true;
	}

	@Override
	public void onDisable() {
		saveConfig();
	}

	public static Main getInstance() {
		return JavaPlugin.getPlugin(Main.class);
	}

	/*
	 * Checks if the player is allowed to place the block (and it is a
	 * silkchest)
	 */
	public boolean placingChecks(BlockPlaceEvent event) {
		ItemStack is = event.getItemInHand();
		if (is != null) {
			if (Utils.isChest(is.getType()) && is.getItemMeta().hasLore()) {
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
		if (!player.hasPermission("silkchest.use") || !Utils.isChest(block.getType())
				|| !player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)
				|| !(block.getState() instanceof Chest))
			return false;
		if (hasWorldGuard && hasLockette) {
			return (WGBukkit.getPlugin().canBuild(player, block) && Lockette.isOwner(block, player));
		} else if (hasWorldGuard) {
			return (WGBukkit.getPlugin().canBuild(player, block));
		} else if (hasLockette) {
			return (Lockette.isOwner(block, player));
		} else {
			return true;
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		if (placingChecks(event)) {

			/* Get deserialized items and put it into the chest */
			Collection<ItemStack> items = Utils.deserialize(Utils.getNBT(event.getItemInHand()));
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
	public void blockBreakDoublechest(BlockBreakEvent event) {
		if (breakingChecks(event) && Utils.isDoubleChest(event.getBlock())) {
			event.setCancelled(true);
			Block block = event.getBlock();

			Chest c = (Chest) block.getState();
			DoubleChest dc = (DoubleChest) c.getInventory().getHolder();

			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();

			DoubleChestInventory inv = (DoubleChestInventory) dc.getInventory();
			Inventory leftInv = inv.getLeftSide();
			Inventory rightInv = inv.getRightSide();
			Inventory chestInv = null;

			if (block.equals(left.getBlock()))
				chestInv = leftInv;
			else if (block.equals(right.getBlock()))
				chestInv = rightInv;
			else
				throw new NullPointerException();

			if (!getConfig().getBoolean("canStoreChestInChest"))
				chestInv = InventoryManager.canStoreChestInChest(chestInv, block);

			InventoryManager.addMetaAndDrop(new ItemStack(Material.CHEST), chestInv, event.getBlock());
		}
	}

	@EventHandler
	public void blockBreakChest(BlockBreakEvent event) {
		if (breakingChecks(event) && !(Utils.isDoubleChest(event.getBlock()))) {
			event.setCancelled(true);
			Chest chest = (Chest) event.getBlock().getState();
			Inventory chestInv = chest.getInventory();
			
			if (!getConfig().getBoolean("canStoreChestInChest"))
				chestInv = InventoryManager.canStoreChestInChest(chestInv, event.getBlock());

			ItemStack is = new ItemStack(Material.CHEST);
			
			if (getConfig().getBoolean("useTrappedChests"))
				if (event.getBlock().getType().equals(Material.TRAPPED_CHEST))
					is = new ItemStack(Material.TRAPPED_CHEST);
			
			InventoryManager.addMetaAndDrop(is, chestInv, event.getBlock());
		}
	}
}
