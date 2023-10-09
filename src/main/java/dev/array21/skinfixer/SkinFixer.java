package dev.array21.skinfixer;

import java.util.HashMap;
import java.util.logging.Logger;

import dev.array21.skinfixer.apis.RemotePluginInfo;
import dev.array21.skinfixer.apis.gson.RemoteInfoManifest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dev.array21.skinfixer.annotations.Nullable;
import dev.array21.skinfixer.commands.CommandHandler;
import dev.array21.skinfixer.config.ConfigHandler;
import dev.array21.skinfixer.config.ConfigManifest;
import dev.array21.skinfixer.discord.JdaHandler;
import dev.array21.skinfixer.events.PlayerJoinEventListener;
import dev.array21.skinfixer.language.LangHandler;
import dev.array21.skinfixer.storage.LibWrapper;
import dev.array21.skinfixer.updatechecker.UpdateChecker;
import net.md_5.bungee.api.ChatColor;

public class SkinFixer extends JavaPlugin {
	private static SkinFixer INSTANCE;
	public static String PLUGIN_VERSION;
	public static Logger LOGGER;
	private final HashMap<Integer, String> skinUuidCodes = new HashMap<>();
	private final HashMap<Integer, String> skinUrlCodes = new HashMap<>();
	private ConfigHandler configHandler;
	private JdaHandler jdaHandler;
	private LibWrapper libWrapper;

	public RemoteInfoManifest remoteInfoManifest;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		PLUGIN_VERSION = this.getDescription().getVersion();
		LOGGER = this.getLogger();

		SkinFixer.logInfo("Welcome to SkinFixer version " + PLUGIN_VERSION + " by TobiasDeBruijn!");
		
		//Read the configuration
		this.configHandler = new ConfigHandler(this);
		ConfigManifest configManifest = configHandler.read();

		this.remoteInfoManifest = RemotePluginInfo.getManifest(this);

		this.libWrapper = new LibWrapper(this);
		this.libWrapper.init();
		
		LangHandler langHandler = new LangHandler(this);
		if(configManifest.language != null) {
			if(!langHandler.loadLang(configManifest.language)) {
				return;
			}
		} else {
			SkinFixer.logWarn("Configuration entry 'language' is missing. Using English as default.");
			langHandler.loadLang("en");
		}

		if(configManifest.updateCheck) {
			new Thread(() -> new UpdateChecker(SkinFixer.this).checkUpdate(), "SkinFixer UpdateChecker Thread").start();
		}

		CommandHandler commandHandler = new CommandHandler(this);
		this.getCommand("skin").setExecutor(commandHandler);
		this.getCommand("skin").setTabCompleter(commandHandler);

		//Setup JDA
		if(configManifest.useDiscord) {
			this.jdaHandler = new JdaHandler(this);
			this.jdaHandler.setupJda();
		}

		//Register event listeners
		Bukkit.getPluginManager().registerEvents(new PlayerJoinEventListener(this), this);

		for(RemoteInfoManifest.Message remoteMessage : this.remoteInfoManifest.messages) {
			logInfo(remoteMessage.text);
		}
	}
	
	public static void logInfo(Object log) {
		LOGGER.info(log.toString());
	}
	
	public static void logWarn(Object log) {
		LOGGER.warning(log.toString());
	}

	/**
	 * Reload the configuration.
	 * @return The new ConfigManifest
	 */
	public ConfigManifest reloadConfigManifest() {
		return this.configHandler.read();
	}
	
	/**
	 * Reload JDA, if Discord is used
	 * - Shutdown JDA
	 * - Re-run JDA setup
	 */
	public void reloadJda() {
		if(this.jdaHandler != null) {
			try {
				this.jdaHandler.shutdownJda();
			} catch(Exception ignored) {}
			this.jdaHandler.setupJda();
		}
	}
	
	/**
	 * Get the JDA Handler. This is null if Discord has been disabled in the config (i.e. it is not used)
	 * @return The JDAHandler, if applicable
	 */
	@Nullable
	public JdaHandler getJdaHandler() {
		return this.jdaHandler;
	}
	
	/**
	 * Get the plugin configuration
	 * @return ConfigManifest
	 */
	public ConfigManifest getConfigManifest() {
		return this.configHandler.getConfigManifest();
	}
	
	/**
	 * Get the libskinfixer wrapper
	 * @return LibWrapper
	 */
	public LibWrapper getLibWrapper() {
		return this.libWrapper;
	}
	
	/**
	 * Get the Skin Code HashMap, where K is the code, and V is the URL associated with the code
	 * @return The Skin URL codes
	 */
	public HashMap<Integer, String> getSkinCodeUrlMap() {
		return this.skinUrlCodes;
	}
	
	public void insertSkinCodeUuid(int code, String uuid) {
		this.skinUuidCodes.put(code, uuid);
	}
	
	public HashMap<Integer, String> getSkinCodeUuidMap() {
		return this.skinUuidCodes;
	}
	
	/**
	 * Insert a new entry into the skin code map
	 * @param skinCode The code to insert
	 * @param url The URL associated with the code
	 */
	public void insertSkinCodeUrl(int skinCode, String url) {
		this.skinUrlCodes.put(skinCode, url);
	}
	
	/**
	 * Get the SkinFixer Prefix which should be used in all messages send to the Player
	 * @return The prefix
	 */
	public static String getPrefix() {
		return ChatColor.GRAY + "[" + ChatColor.AQUA + "SF" + ChatColor.GRAY + "] " + ChatColor.RESET;
	}
}
