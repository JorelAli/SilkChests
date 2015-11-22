package io.github.skepter.silkchests;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;

public class ReflectionUtils {
	
	private final String packageName;
	private final String obcPackageName;
	private final Object dedicatedServer;
	private final Object craftServer;
	public final Class<?> craftItemStack;

	public ReflectionUtils() throws Exception {
		this.obcPackageName = Bukkit.getServer().getClass().getPackage().getName();
		this.craftServer = getOBCClass("CraftServer").cast(Bukkit.getServer());
		this.dedicatedServer = getPrivateFieldValue(this.craftServer, "console");
		this.packageName = this.dedicatedServer.getClass().getPackage().getName();
		this.craftItemStack = getOBCClass("inventory.CraftItemStack");
	}

	public Class<?> getNMSClass(String className) throws ClassNotFoundException {
		return Class.forName(this.packageName + "." + className);
	}

	public Class<?> getOBCClass(String className) throws ClassNotFoundException {
		return Class.forName(this.obcPackageName + "." + className);
	}

	public static Object getPrivateFieldValue(Object object, String fieldName) throws Exception {
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(object);
	}
}
