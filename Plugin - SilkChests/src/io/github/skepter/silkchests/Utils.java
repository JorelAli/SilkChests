package io.github.skepter.silkchests;

import org.bukkit.inventory.ItemStack;

public class Utils {

	/* Writes NBT data to an itemstack */
	public static ItemStack setNBT(ItemStack is, String serializedString) {
		try {
			ReflectionUtils utils = new ReflectionUtils();
			Object nmsItem = new ReflectionUtils().craftItemStack.getDeclaredMethod("asNMSCopy", new Class[] { ItemStack.class }).invoke(null, new Object[] { is });

			Object tag = ReflectionUtils.getPrivateFieldValue(nmsItem, "tag");
			if (tag == null) {
				tag = utils.getNMSClass("NBTTagCompund").newInstance();
			}
			tag.getClass().getDeclaredMethod("setString", new Class[] { String.class, String.class }).invoke(tag, new Object[] { "silkTouchItems", serializedString });

			ItemStack newItemStack = (ItemStack) new ReflectionUtils().craftItemStack.getDeclaredMethod("asCraftMirror", new Class[] { nmsItem.getClass() }).invoke(null, new Object[] { nmsItem });
			return newItemStack;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	public static String getNBT(ItemStack is) {
		try {
			Object nmsItem = new ReflectionUtils().craftItemStack.getDeclaredMethod("asNMSCopy", new Class[] { ItemStack.class }).invoke(null, new Object[] { is });

			Object tag = ReflectionUtils.getPrivateFieldValue(nmsItem, "tag");
			String seralizedString = (String) tag.getClass().getDeclaredMethod("getString", new Class[] { String.class }).invoke(tag, new Object[] { "silkTouchItems" });
			return seralizedString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
