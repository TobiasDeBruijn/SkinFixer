package nl.thedutchmc.SkinFixer.fileHandlers;

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

import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.SkinObject;

public class StorageHandler {
	
	public static HashMap<Integer, String> pendingLinks = new HashMap<>();
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
		
		if(this.getConfig().getList("pendingLinks") != null) {
			List<String> pendingLinksAsList = (List<String>) this.getConfig().getList("pendingLinks");
			
			for(String encoded : pendingLinksAsList) {
				final String[] parts = encoded.split("<--->");
				pendingLinks.put(Integer.valueOf(parts[0]), parts[1]);
			}
		}

		if(this.getConfig().getList("skinData") != null) {
			List<String> skinDataAsList = (List<String>) this.getConfig().getList("skinData");
			
			for(String encoded : skinDataAsList) {
							
				String[] parts = encoded.split("<--->");
								
				char[] ca = parts[0].toCharArray();
            	
            	//Remove trailing spaces
            	String uuid = "";
            	for(int i = 0; i < ca.length; i++) {
            		char c = ca[i];
            		
            		if(i == (ca.length -1) && c == ' ') continue;
            			
            		uuid += c;
            	}
            					
				skins.put(
					UUID.fromString(uuid), //uuid
					new SkinObject(
						UUID.fromString(uuid), //uuid
						parts[1], //value
						parts[2]  //signature
					)
				);
			}
		}
	}
	
	public void updateConfig() {
		
		List<String> pendingLinksAsList = new ArrayList<>();
		for(Map.Entry<Integer, String> entry : pendingLinks.entrySet()) {
			pendingLinksAsList.add(entry.getKey().toString() + "<--->" + entry.getValue());
		}
		
		List<String> skinDataAsList = new ArrayList<>();
		for(Map.Entry<UUID, SkinObject> entry : skins.entrySet()) {
			String uuid = entry.getKey().toString();
			
			String value = entry.getValue().getValue();
			String signature = entry.getValue().getSignature();
			
			skinDataAsList.add(uuid.toString() + "<--->" + value + "<--->" + signature);
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
