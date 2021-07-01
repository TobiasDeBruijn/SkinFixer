package dev.array21.skinfixer.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.Files;
import com.google.gson.Gson;

import dev.array21.classvalidator.ClassValidator;
import dev.array21.classvalidator.Pair;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.util.Utils;

public class LangHandler {

	private SkinFixer plugin;
	
	public static LanguageModel model;
	
	public LangHandler(SkinFixer plugin) {
		this.plugin = plugin;
		
		File langFolder = new File(this.plugin.getDataFolder() + File.separator + "langs");
		if(!langFolder.exists()) {
			langFolder.mkdirs();
		}
	}
	
	public void loadLang(String lang) {
		File langFile = new File(this.plugin.getDataFolder() + File.separator + "langs", lang + ".yml");
		if(!langFile.exists()) {
			SkinFixer.logWarn("Failed to load language model " + lang + ".yml!");
			langFile = getEngModel();
		}
		
		LanguageModel model = loadModel(langFile);
		LangHandler.model = model;
	}
	
	private File getEngModel() {
		File langFile = new File(this.plugin.getDataFolder() + File.separator + "langs", "en.yml");
		if(!langFile.exists()) {
			SkinFixer.logInfo("Language model 'en.yml' does not exist. Saving from JAR.");
			this.plugin.saveResource("en.yml", false);
			
			try {
				Files.move(new File(this.plugin.getDataFolder(), "en.yml"), langFile);
			} catch(IOException e) {
				SkinFixer.logWarn("Failed to save language model 'en.yml': " + e.getMessage());
				SkinFixer.logWarn(Utils.getStackTrace(e));
				
				Bukkit.getPluginManager().disablePlugin(this.plugin);
				return null;
			}
		}
		
		return langFile;
	}
	
	private LanguageModel loadModel(File modelFile) {
		final Yaml yaml = new Yaml();
		final Gson gson = new Gson();
		
		Object yamlData;
		try {
			yamlData = yaml.load(new FileInputStream(modelFile));
		} catch(FileNotFoundException e) {
			SkinFixer.logWarn(String.format("Failed to load LanguuageModel '%s'. It does not exist.", modelFile.getAbsolutePath()));
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return null;
		}
		
		String jsonData = gson.toJson(yamlData, LinkedHashMap.class);
		LanguageModel model = gson.fromJson(jsonData, LanguageModel.class);
		
		Pair<Boolean, String> validationResult = ClassValidator.validateType(model);
		if(validationResult.getA() == null) {
			SkinFixer.logWarn("Failed to validate language model: " + validationResult.getB());
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return null;
		}
		
		if(!validationResult.getA() ) {
			SkinFixer.logWarn(String.format("LanguageModel '%s' failed validation: %s", modelFile.getAbsolutePath(), validationResult.getB()));
			SkinFixer.logWarn("Did you recently update SkinFixer and forgot to remove your 'langs' folder?");
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return null;
		}
		
		return model;
	}
}
