package dev.array21.skinfixer.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

import dev.array21.classvalidator.ClassValidator;
import dev.array21.classvalidator.Pair;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.util.Utils;

public class ConfigHandler {

	private SkinFixer plugin;
	private ConfigManifest manifest;
	
	private final static Yaml YAML = new Yaml();
	private final static Gson GSON = new Gson();
	
	public ConfigHandler(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	public ConfigManifest read() {
		File configFile = new File(this.plugin.getDataFolder(), "config.yml");
		if(!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			this.plugin.saveResource("config.yml", false);
		}
		
		Object yaml;
		try {
			yaml = YAML.load(new FileInputStream(configFile));
		} catch(FileNotFoundException e) {
			return null;
		}
		
		String json = GSON.toJson(yaml, java.util.LinkedHashMap.class);
		ConfigManifest manifest = GSON.fromJson(json, ConfigManifest.class);
		
		Pair<Boolean, String> validation = ClassValidator.validateType(manifest);
		if(validation.getA() == null) {
			SkinFixer.logWarn(String.format("Failed to validate configuration file due to a fatal error: %s", validation.getB()));
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return null;
		}
		
		if(!validation.getA()) {
			SkinFixer.logWarn(String.format("Configuration is invalid: %", validation.getB()));
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return null;
		}
		
		if(manifest.statUuid == null) {
			manifest.statUuid = "";
		}
		
		if(manifest.useDiscord != null && manifest.useDiscord) {
			if(manifest.token == null || manifest.token.isEmpty()) {
				SkinFixer.logWarn("Configuration is invalid: useDiscord is set to true, but 'token' was left blank.");
				Bukkit.getPluginManager().disablePlugin(this.plugin);
				return null;
			}
			
			if(manifest.channel == null || manifest.channel.isEmpty()) {
				SkinFixer.logWarn("Configuration is invalid: useDiscord is set to true, but 'channel' was left blank.");
				Bukkit.getPluginManager().disablePlugin(this.plugin);
				return null;
			}
		}
		
		this.manifest = manifest;
		return manifest;
	}
	
	public void setStatUuid(String uuid) {
		this.manifest.statUuid = uuid;
		String yaml = YAML.dumpAsMap(this.manifest);
		try {
			File configFile = new File(this.plugin.getDataFolder(), "config.yml");
			BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
			bw.write(yaml);
			bw.flush();
			bw.close();
		} catch(IOException e) {
			SkinFixer.logWarn(String.format("Failed to write statUuid to disk: %s", e.getMessage()));
			SkinFixer.logWarn(Utils.getStackTrace(e));
		}
	}
}
