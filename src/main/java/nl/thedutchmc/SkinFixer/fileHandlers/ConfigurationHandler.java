package nl.thedutchmc.SkinFixer.fileHandlers;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import nl.thedutchmc.SkinFixer.SkinFixer;

public class ConfigurationHandler {

	public static boolean useDiscord;
	public static String token, channel;
	
	/**
	 * @since 1.4.0
	 */
	public static String language;
	
	/**
	 * @since 1.4.1
	 */
	public static String statUuid;
	
	/**
	 * @since 1.4.1
	 */
	public static boolean disableStat;
	
	private File file;
	private FileConfiguration config;
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public void loadConfig() {
		file = new File(SkinFixer.INSTANCE.getDataFolder(), "config.yml");
		
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			SkinFixer.INSTANCE.saveResource("config.yml", false);
		}
		
		config = new YamlConfiguration();
		
		try {
			config.load(file);
			readConfig();
		} catch (InvalidConfigurationException e) {
			SkinFixer.logWarn("Invalid config.yml!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readConfig() {
		useDiscord = this.getConfig().getBoolean("useDiscord");
		
		if(useDiscord) {
			token = this.getConfig().getString("discordToken");
			channel = this.getConfig().getString("skinDiscordChannel");
			
			if(token.equals("") || channel.equals("")) {
				SkinFixer.logWarn("Token or channel has not been filled in. Not using Discord!");
				useDiscord = false;
			}
		}
		
		language = this.getConfig().getString("language");
		
		String uuid = this.getConfig().getString("statUuid");
		statUuid = uuid != null ? uuid : "";
		disableStat = this.getConfig().getBoolean("disableStat");
		
	}
	
	public void setUuid(String uuid) {
		ConfigurationHandler.statUuid = uuid;
		
		this.getConfig().set("statUuid", uuid);
		try {
			this.config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
