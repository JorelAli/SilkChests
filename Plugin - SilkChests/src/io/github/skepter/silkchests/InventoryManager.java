package io.github.skepter.silkchests;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryManager {

	public static Inventory canStoreChestInChest(Inventory chestInv, Block block) {
		/* Removes the SilkChests from the chest and drops them */
		for (int i = 0; i < chestInv.getContents().length; i++) {
			ItemStack is = chestInv.getItem(i);
			if (is != null) {
				if (Utils.isChest(is.getType()) && is.hasItemMeta()) {
					if (is.getItemMeta().hasLore()) {
						if (!is.getItemMeta().getLore().isEmpty()) {
							if (is.getItemMeta().getLore().get(0).equals("SilkChest")) {
								block.getWorld().dropItem(block.getLocation(), is);
								chestInv.remove(is);
							}
						}
					}
				}
			}
		}
		return chestInv;
	}
	
	public static void addMetaAndDrop(ItemStack is, Inventory chestInv, Block block) {
		String serializedString = Utils.serialize(Arrays.asList(chestInv.getContents()));

		ItemMeta meta = is.getItemMeta();
		meta.setLore(Arrays.asList(new String[] { "SilkChest" }));
		is.setItemMeta(meta);

		ItemStack newItemStack = Utils.setNBT(is, serializedString);
		block.getWorld().dropItem(block.getLocation(), newItemStack);
		chestInv.clear();
		block.setType(Material.AIR);
	}
	
}
