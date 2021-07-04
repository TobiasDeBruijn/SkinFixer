package dev.array21.skinfixer.rust;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.regex.Pattern;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.config.ConfigManifest;
import dev.array21.skinfixer.config.SqlSettings;
import dev.array21.skinfixer.storage.SkinData;

public class LibWrapper {
	
	private static boolean LIB_LOADED = false;
	
	static {
		saveLib: {
			String osString = System.getProperty("os.name").toLowerCase();
			String libName;
			if(osString.contains("linux")) {
				libName = "/x86_64/linux/libskinfixer.so";
			} else if(osString.contains("windows")) {
				libName = "/x86_64/linux/libskinfixer.dll";
			} else if(osString.contains("mac")) {
				libName = "/x86_64/linux/libskinfixer.dylib";
			} else {
				SkinFixer.logWarn(String.format("Your operating system is not supported. Please open a request here: https://github.com/TheDutchMC/SkinFixer/issues/new/choose. Your OS is '%s', make sure you mention this in your request!", osString));
				break saveLib;
			}
			
			URL libUrl = LibWrapper.class.getResource(libName);
			File tmpDir;
			try {
				tmpDir = Files.createTempDirectory("libskinfixer").toFile();
			} catch(IOException e) {
				SkinFixer.logWarn("Failed to create temporary directory for library: " + e.getMessage());
				break saveLib;
			}
			
			String[] libNameParts = libName.split(Pattern.quote("/"));
			File libTmpFile = new File(tmpDir, libNameParts[libName.length() -1]);
			try {
				InputStream is = libUrl.openStream();
				Files.copy(is, libTmpFile.toPath());
			} catch(IOException e) {
				SkinFixer.logWarn("Failed to save library to temporary directory: " + e.getMessage());
				tmpDir.delete();
				break saveLib;
			}
			
			libTmpFile.deleteOnExit();
			tmpDir.deleteOnExit();
			
			try {
				System.load(libTmpFile.getAbsolutePath());
			} catch(UnsatisfiedLinkError e) {
				SkinFixer.logWarn("Failed to load library: " + e.getMessage());
				libTmpFile.delete();
				tmpDir.delete();
				break saveLib;
			}
			
			LIB_LOADED = true;
		}
	}
	
	private SkinFixer plugin;
	
	public LibWrapper(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	public void init() {
		if(!LIB_LOADED) {
			return;
		}
		
		ConfigManifest configManifest = this.plugin.getConfigManifest();
		SqlSettings sqlSettings = configManifest.sqlSettings;
		
		String host = (sqlSettings.host != null) ? sqlSettings.host : "";
		String database = (sqlSettings.database != null) ? sqlSettings.database : "";
		String username = (sqlSettings.username != null) ? sqlSettings.username : "";
		String password = (sqlSettings.password != null) ? sqlSettings.password : "";
		
		File pluginFolder = this.plugin.getDataFolder();
		if(!pluginFolder.exists()) {
			pluginFolder.mkdirs();
		}
		
		LibSkinFixer.init(configManifest.databaseType.toString(), host, database, username, password, pluginFolder.getAbsolutePath());
	}
	
	public SkinData getSkinProfile(UUID owner) {
		if(!LIB_LOADED) {
			return null;
		}
		
		// Because working with primitives and Strings is way easier than Java objects
		// over JNI, we use a String[] to communicate the values
		// [0]: value
		// [1]: signature
		// If the array is empty, no results were found
		String[] databaseResult = LibSkinFixer.getSkinProfile(owner.toString());
		if(databaseResult.length == 0) {
			return null;
		}
		
		String value = databaseResult[0];
		String signature = databaseResult[1];
		SkinData sd = new SkinData();
		sd.playerUuid = owner.toString();
		sd.value = value;
		sd.signature = signature;
		
		return sd;
	}
	
	public void setSkinProfile(SkinData sd) {
		if(!LIB_LOADED) {
			return;
		}
		
		LibSkinFixer.setSkinProfile(sd.playerUuid, sd.value, sd.signature);
	}
	
	public void delSkinProfile(UUID owner) {
		if(!LIB_LOADED) {
			return;
		}
		
		LibSkinFixer.delSkinProfile(owner.toString());
	}
}
