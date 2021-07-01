package dev.array21.skinfixer;

import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dev.array21.pluginstatlib.PluginStat;
import dev.array21.pluginstatlib.PluginStat.PluginStatBuilder;
import dev.array21.skinfixer.commands.GetCodeCommandExecutor;
import dev.array21.skinfixer.commands.ResetSkinCommandExecutor;
import dev.array21.skinfixer.commands.SetSkinCommandExecutor;
import dev.array21.skinfixer.commands.SkinFixerCommandExecutor;
import dev.array21.skinfixer.config.ConfigHandler;
import dev.array21.skinfixer.config.ConfigManifest;
import dev.array21.skinfixer.discord.JdaHandler;
import dev.array21.skinfixer.events.PlayerJoinEventListener;
import dev.array21.skinfixer.language.LangHandler;
import dev.array21.skinfixer.storage.StorageHandler;
import dev.array21.skinfixer.updatechecker.UpdateChecker;

public class SkinFixer extends JavaPlugin {

	private static SkinFixer INSTANCE;
	public static String PLUGIN_VERSION;
	public static final Logger LOGGER = LogManager.getLogger(SkinFixer.class);
	
	private HashMap<Integer, String> skinCodes = new HashMap<>();
	
	private ConfigManifest configManifest;
	private StorageHandler storageHandler;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		PLUGIN_VERSION = this.getDescription().getVersion();
		
		SkinFixer.logInfo("Welcome to SkinFixer version " + PLUGIN_VERSION + " by TheDutchMC!");
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				new UpdateChecker(SkinFixer.this).checkUpdate();

			}
		}, "SkinFixer UpdateChecker Thread").start();
		
		//Read the configuration
		ConfigHandler configHandler = new ConfigHandler(this);
		this.configManifest = configHandler.read();
	
		LangHandler langHandler = new LangHandler(this);
		if(this.configManifest.language != null) {
			langHandler.loadLang(this.configManifest.language);
		} else {
			SkinFixer.logWarn("Configuration entry 'language' is missing. Using English as default.");
			langHandler.loadLang("en");
		}
		
		if(!this.configManifest.disableStat) {
			PluginStat stat = PluginStatBuilder.createDefault()
					.setLogErrFn(SkinFixer::logWarn)
					.setSetUuidFn(configHandler::setStatUuid)
					.setUuid(this.configManifest.statUuid)
					.build();
			
			stat.start();
		}
		
		//Storage of skins and pending keys
		this.storageHandler = new StorageHandler(this);
		this.storageHandler.read();	
		
		//Register command executors
		this.getCommand("setskin").setExecutor(new SetSkinCommandExecutor(this));
		this.getCommand("getcode").setExecutor(new GetCodeCommandExecutor(this));
		this.getCommand("skinfixer").setExecutor(new SkinFixerCommandExecutor());
		this.getCommand("resetskin").setExecutor(new ResetSkinCommandExecutor(this));
		
		
		//Setup JDA
		if(this.configManifest.useDiscord) {
			JdaHandler jdaHandler = new JdaHandler(this);
			jdaHandler.setupJda();
		}

		//Register event listeners
		Bukkit.getPluginManager().registerEvents(new PlayerJoinEventListener(this), this);
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
	
	public ConfigManifest getConfigManifest() {
		return this.configManifest;
	}
	
	public StorageHandler getStorageHandler() {
		return this.storageHandler;
	}
	
	public HashMap<Integer, String> getSkinCodeMap() {
		return this.skinCodes;
	}
	
	public void insertSkinCode(int skinCode, String url) {
		this.skinCodes.put(skinCode, url);
	}
}
