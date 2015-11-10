package io.github.skepter.silkchests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
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

public class Main extends JavaPlugin implements Listener {
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		if ((event.getItemInHand().getType().equals(Material.CHEST)) && (event.getItemInHand().getItemMeta().getLore().contains("SilkChest"))) {
			
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
		if ((event.getPlayer().hasPermission("silkchest.use")) && (event.getBlock().getType().equals(Material.CHEST)) && (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH))
				&& ((event.getBlock().getState() instanceof Chest))) {

			/* Serialize chest contents */
			Player player = event.getPlayer();
			Chest chest = (Chest) event.getBlock().getState();
			event.setCancelled(true);
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
