package nl.thedutchmc.SkinFixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StorageHandler {
	
	public static HashMap<String, String> pendingLinks = new HashMap<>();
	public static HashMap<UUID, SkinObject> skins = new HashMap<>();
	
	private File file;
	private FileConfiguration config;
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public void loadConfig() {
		file = new File(SkinFixer.INSTANCE.getDataFolder(), "storage.yml");
		
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			SkinFixer.INSTANCE.saveResource("storage.yml", false);
		}
		
		config = new YamlConfiguration();
		
		try {
			config.load(file);
			readConfig();
		} catch (InvalidConfigurationException e) {
			SkinFixer.logWarn("Invalid storage.yml!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void readConfig() {
		List<String> pendingLinksAsList = (List<String>) this.getConfig().getList("pendingLinks");
		
		for(String encoded : pendingLinksAsList) {
			final String[] parts = encoded.split("|");
			pendingLinks.put(parts[0], parts[1]);
		}
		
		List<String> skinDataAsList = (List<String>) this.getConfig().getList("skinData");
		
		for(String encoded : skinDataAsList) {
			final String[] parts = encoded.split("|");
			
			skins.put(
				UUID.fromString(parts[0]), //uuid
				new SkinObject(
					UUID.fromString(parts[0]), //uuid
					parts[1], //value
					parts[2]  //signature
				)
			);
		}
	}
	
	public void updateConfig() {
		
		List<String> pendingLinksAsList = new ArrayList<>();
		for(Map.Entry<String, String> entry : pendingLinks.entrySet()) {
			pendingLinksAsList.add(entry.getKey() + "|" + entry.getValue());
		}
		
		List<String> skinDataAsList = new ArrayList<>();
		for(Map.Entry<UUID, SkinObject> entry : skins.entrySet()) {
			String uuid = entry.getKey().toString();
			
			String value = entry.getValue().getValue();
			String signature = entry.getValue().getSignature();
			
			skinDataAsList.add(uuid + "|" + value + "|" + signature);
		}
		
		this.getConfig().set("pendingLinks", pendingLinksAsList);
		this.getConfig().set("skinData", skinDataAsList);
		
		try {
			this.getConfig().save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
