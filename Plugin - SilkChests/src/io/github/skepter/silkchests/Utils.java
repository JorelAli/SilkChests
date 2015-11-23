package io.github.skepter.silkchests;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class Utils {

	/* Writes NBT data to an itemstack */
	protected static ItemStack setNBT(ItemStack is, String serializedString) {
		try {
			ReflectionUtils utils = new ReflectionUtils();
			Object nmsItem = new ReflectionUtils().craftItemStack.getDeclaredMethod("asNMSCopy",
					new Class[] { ItemStack.class }).invoke(null, new Object[] { is });

			Object tag = ReflectionUtils.getPrivateFieldValue(nmsItem, "tag");
			if (tag == null) {
				tag = utils.getNMSClass("NBTTagCompund").newInstance();
			}
			tag.getClass().getDeclaredMethod("setString", new Class[] { String.class, String.class })
					.invoke(tag, new Object[] { "silkTouchItems", serializedString });

			ItemStack newItemStack = (ItemStack) new ReflectionUtils().craftItemStack.getDeclaredMethod(
					"asCraftMirror", new Class[] { nmsItem.getClass() }).invoke(null, new Object[] { nmsItem });
			return newItemStack;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	/* Gets the NBT data from an item */
	protected static String getNBT(ItemStack is) {
		try {
			Object nmsItem = new ReflectionUtils().craftItemStack.getDeclaredMethod("asNMSCopy",
					new Class[] { ItemStack.class }).invoke(null, new Object[] { is });

			Object tag = ReflectionUtils.getPrivateFieldValue(nmsItem, "tag");
			String seralizedString = (String) tag.getClass()
					.getDeclaredMethod("getString", new Class[] { String.class })
					.invoke(tag, new Object[] { "silkTouchItems" });
			return seralizedString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/* Checks if the item is a SilkItem (e.g. a silkchest) */
	protected static boolean isSilkItem(ItemStack is) {
		return getNBT(is) != null;
	}
	

	/* Turns item stacks into a string */
	protected static String serialize(List<ItemStack> items) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("Items", items);
		return Base64Coder.encodeString(config.saveToString());
	}

	/* Converts the string back into itemstacks */
	@SuppressWarnings("unchecked")
	protected static List<ItemStack> deserialize(String s) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(Base64Coder.decodeString(s));
			return (List<ItemStack>) config.get("Items");
		} catch (InvalidConfigurationException e) {
		}
		return null;
	}
	
	/* Checks if the block is a chest or a trapped chest */
	protected static boolean isChest(Material material) {
		if (Main.trappedChests) {
			return (material.equals(Material.CHEST) || material.equals(Material.TRAPPED_CHEST));
		} else {
			return (material.equals(Material.CHEST));
		}
	}

	/* Checks if the block is a doublechest */
	protected static boolean isDoubleChest(Block block) {
		if (block.getState() instanceof Chest) {
			Chest c = (Chest) block.getState();
			return (c.getInventory().getHolder() instanceof DoubleChest);
		}
		return false;
	}
	
	/** Centers text (From AllAssets) */
	protected static String center(final String text) {
		final int spaces = (int) Math.round((80 - (1.4 * ChatColor.stripColor(text).length())) / 2);
		String s = "";
		for (int i = 0; i < spaces; i++)
			s = s + " ";
		return s + text;
	}

}
