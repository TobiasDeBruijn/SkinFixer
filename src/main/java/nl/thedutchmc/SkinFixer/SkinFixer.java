package nl.thedutchmc.SkinFixer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import nl.thedutchmc.SkinFixer.commandHandlers.SetSkinCommandExecutor;

public class SkinFixer extends JavaPlugin {

	public static SkinFixer INSTANCE;
	public static StorageHandler STORAGE;
	
	public static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		SkinFixer.logInfo("Using NMS Version " + SkinFixer.NMS_VERSION);
		
		//Read the configuration
		ConfigurationHandler configHandler = new ConfigurationHandler();
		configHandler.loadConfig();
	
		//Storage of skins and pending keys
		STORAGE = new StorageHandler();
		STORAGE.readConfig();
		
		if(Bukkit.getOnlineMode() == true) logInfo("This plugin is not needed on servers running in online mode!");
		
		//Register command executors
		this.getCommand("setskin").setExecutor(new SetSkinCommandExecutor());
		
		//Setup JDA
		JdaHandler jdaHandler = new JdaHandler();
		jdaHandler.setupJda();
		
		//TODO EventListeners for joining
		
	}
	
	@Override
	public void onDisable() {
		//TODO Log out JDA to quicken shutdown
	}
	
	public static void logInfo(String log) {
		Bukkit.getLogger().info("[" + SkinFixer.INSTANCE.getDescription().getName() + "] " + log);	
	}
	
	public static void logWarn(String log) {
		Bukkit.getLogger().warning("[" + SkinFixer.INSTANCE.getDescription().getName() + "] " + log);	
	}
}
