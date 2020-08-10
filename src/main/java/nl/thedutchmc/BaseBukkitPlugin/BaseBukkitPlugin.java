package nl.thedutchmc.BaseBukkitPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BaseBukkitPlugin extends JavaPlugin {

	public static BaseBukkitPlugin INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		ConfigurationHandler configHandler = new ConfigurationHandler();
		configHandler.loadConfig();
	
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public static void logInfo(String log) {
		Bukkit.getLogger().info("[" + BaseBukkitPlugin.INSTANCE.getDescription().getName() + "] " + log);	
	}
	
	public static void logWarn(String log) {
		Bukkit.getLogger().warning("[" + BaseBukkitPlugin.INSTANCE.getDescription().getName() + "] " + log);	
	}
}
