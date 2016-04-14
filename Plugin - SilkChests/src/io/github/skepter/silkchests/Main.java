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
	 */
	private boolean hasLockette = false;
	private boolean hasWorldGuard = false;

	protected static boolean trappedChests = true;
	protected static boolean chestInChest = false;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		if (Bukkit.getPluginManager().getPlugin("Lockette") != null)
			hasLockette = true;
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
			hasWorldGuard = true;
		getCommand("silkchests").setExecutor(new SilkChestsCommand(this));
		updateInternalConfig();

	}

	/*
	 * Internal config is updated based on config.yml file
	 */
	protected void updateInternalConfig() {
		trappedChests = getConfig().getBoolean("useTrappedChests");
		chestInChest = getConfig().getBoolean("canStoreChestInChest");
	}

	protected void updateExternalConfig() {
		getConfig().set("useTrappedChests", trappedChests);
		getConfig().set("canStoreChestInChest", chestInChest);
		saveConfig();
	}

	@Override
	public void onDisable() {
		saveConfig();
	}

	/*
	 * Checks if the player is allowed to place the block (and it is a
	 * silkchest)
	 */
	protected boolean placingChecks(BlockPlaceEvent event) {
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
	protected boolean breakingChecks(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (!player.hasPermission("silkchest.use") || !Utils.isChest(block.getType())
				|| !player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) || !(block.getState() instanceof Chest))
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
			if (items != null) {
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
	}

	@EventHandler
	public void doubleChestBreak(BlockBreakEvent event) {
		if (breakingChecks(event) && Utils.isDoubleChest(event.getBlock())) {
			event.setCancelled(true);
			Block block = event.getBlock();

			DoubleChest dc = (DoubleChest) ((Chest) block.getState()).getInventory().getHolder();

			DoubleChestInventory inv = (DoubleChestInventory) dc.getInventory();
			Inventory chestInv = null;

			if (block.equals(((Chest) dc.getLeftSide()).getBlock()))
				chestInv = inv.getLeftSide();
			else if (block.equals(((Chest) dc.getRightSide()).getBlock()))
				chestInv = inv.getRightSide();
			else
				throw new NullPointerException();

			if (!chestInChest)
				chestInv = InventoryManager.canStoreChestInChest(chestInv, block);

			ItemStack is = new ItemStack(Material.CHEST);

			if (trappedChests)
				if (event.getBlock().getType().equals(Material.TRAPPED_CHEST))
					is = new ItemStack(Material.TRAPPED_CHEST);

			InventoryManager.addMetaAndDrop(is, chestInv, event.getBlock());
		}
	}

	@EventHandler
	public void chestBreak(BlockBreakEvent event) {
		if (breakingChecks(event) && !(Utils.isDoubleChest(event.getBlock()))) {
			event.setCancelled(true);
			Chest chest = (Chest) event.getBlock().getState();
			Inventory chestInv = chest.getInventory();

			if (!chestInChest)
				chestInv = InventoryManager.canStoreChestInChest(chestInv, event.getBlock());

			ItemStack is = new ItemStack(Material.CHEST);

			if (trappedChests)
				if (event.getBlock().getType().equals(Material.TRAPPED_CHEST))
					is = new ItemStack(Material.TRAPPED_CHEST);

			InventoryManager.addMetaAndDrop(is, chestInv, event.getBlock());
		}
	}
}
