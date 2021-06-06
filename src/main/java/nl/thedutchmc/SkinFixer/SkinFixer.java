package nl.thedutchmc.SkinFixer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dev.array21.pluginstatlib.PluginStat;
import dev.array21.pluginstatlib.PluginStat.PluginStatBuilder;
import nl.thedutchmc.SkinFixer.commandexecutors.GetCodeCommandExecutor;
import nl.thedutchmc.SkinFixer.commandexecutors.SetSkinCommandExecutor;
import nl.thedutchmc.SkinFixer.commandexecutors.SkinFixerCommandExecutor;
import nl.thedutchmc.SkinFixer.fileHandlers.ConfigurationHandler;
import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;
import nl.thedutchmc.SkinFixer.language.LangHandler;
import nl.thedutchmc.SkinFixer.minecraftevents.PlayerJoinEventListener;
import nl.thedutchmc.SkinFixer.updatechecker.UpdateChecker;

public class SkinFixer extends JavaPlugin {

	public static SkinFixer INSTANCE;
	public static StorageHandler STORAGE;
	public static String PLUGIN_VERSION;
	
	public static final Logger LOGGER = LogManager.getLogger(SkinFixer.class);
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		PLUGIN_VERSION = Bukkit.getPluginManager().getPlugin("SkinFixer").getDescription().getVersion();
		
		SkinFixer.logInfo("Welcome to SkinFixer version " + PLUGIN_VERSION + " by TheDutchMC!");
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				new UpdateChecker(SkinFixer.this).checkUpdate();

			}
		}, "SkinFixer UpdateChecker Thread").start();
		
		//Read the configuration
		ConfigurationHandler configHandler = new ConfigurationHandler();
		configHandler.loadConfig();
	
		LangHandler langHandler = new LangHandler(this);
		if(ConfigurationHandler.language != null) {
			langHandler.loadLang(ConfigurationHandler.language);
		} else {
			SkinFixer.logWarn("Configuration entry 'language' is missing. Using English as default.");
			langHandler.loadLang("en");
		}
		
		if(!ConfigurationHandler.disableStat) {
			PluginStat stat = PluginStatBuilder.createDefault()
					.setLogErrFn(SkinFixer::logWarn)
					.setSetUuidFn(configHandler::setUuid)
					.setUuid(ConfigurationHandler.statUuid)
					.build();
			
			stat.start();
		}
		
		//Storage of skins and pending keys
		STORAGE = new StorageHandler();
		STORAGE.loadConfig();
				
		//Register command executors
		this.getCommand("setskin").setExecutor(new SetSkinCommandExecutor());
		this.getCommand("getcode").setExecutor(new GetCodeCommandExecutor());
		this.getCommand("skinfixer").setExecutor(new SkinFixerCommandExecutor());
		
		//Setup JDA
		if(ConfigurationHandler.useDiscord) {
			JdaHandler jdaHandler = new JdaHandler();
			jdaHandler.setupJda();
		}

		//Register event listeners
		Bukkit.getPluginManager().registerEvents(new PlayerJoinEventListener(), this);
	}
	
	@Override
	public void onDisable() {
		logInfo("Shutting down JDA");
		
		try {
			JdaHandler.shutdownJda();
		} catch(Exception e) {}
		
		logInfo("Thank you for using SkinFixer by TheDutchMC");
	}
	
	public static void logInfo(Object log) {
		Bukkit.getLogger().info("[" + SkinFixer.INSTANCE.getDescription().getName() + "] " + log.toString());	
	}
	
	public static void logWarn(Object log) {
		Bukkit.getLogger().warning("[" + SkinFixer.INSTANCE.getDescription().getName() + "] " + log.toString());	
	}
}
