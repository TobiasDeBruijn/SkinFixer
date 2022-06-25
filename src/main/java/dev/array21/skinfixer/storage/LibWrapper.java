package dev.array21.skinfixer.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.regex.Pattern;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.annotations.Nullable;
import dev.array21.skinfixer.config.ConfigManifest;
import dev.array21.skinfixer.config.SqlSettings;
import dev.array21.skinfixer.util.Pair;
import dev.array21.skinfixer.util.Utils;

public class LibWrapper {
	
	private static boolean LIB_LOADED = false;
	
	static {
		saveLib: {
			String libName;
			
			String osName = System.getProperty("os.name").toLowerCase();
			if(osName.contains("linux")) {
				switch(System.getProperty("os.arch")) {
				case "amd64": libName = "/x86_64/linux/libskinfixer.so"; break;
				case "arm": libName = "/armhf/linux/libskinfixer.so"; break;
				case "aarch64": libName = "/aarch64/linux/libskinfixer.so"; break;
				default:
					SkinFixer.logWarn(String.format("Your architecture is not supported. Please open a request here: https://github.com/TheDutchMC/SkinFixer/issues/new/choose. Your Arch is '%s' running on Linux, make sure you mention this in your request!", System.getProperty("os.arch")));
					break saveLib;
				}
				
			} else if(osName.contains("windows")) {
				switch(System.getProperty("os.arch")) {
				case "amd64": libName = "/x86_64/windows/libskinfixer.dll"; break;
				default:
					SkinFixer.logWarn(String.format("Your architecture is not supported. Please open a request here: https://github.com/TheDutchMC/SkinFixer/issues/new/choose. Your Arch is '%s' running on Windows, make sure you mention this in your request!", System.getProperty("os.arch")));
					break saveLib;
				}
				
			} else if(osName.contains("mac")) {
				switch(System.getProperty("os.arch")) {
				case "amd64": libName = "/x86_64/darwin/libskinfixer.dylib";; break;
				case "aarch64": libName = "/aarch64/darwin/libskinfixer.dylib";; break;
				default:
					SkinFixer.logWarn(String.format("Your architecture is not supported. Please open a request here: https://github.com/TheDutchMC/SkinFixer/issues/new/choose. Your Arch is '%s' running on MacOS (Apple Darwin), make sure you mention this in your request!", System.getProperty("os.arch")));
					break saveLib;
				}			
			} else {
				SkinFixer.logWarn(String.format("Your operating system is not supported. Please open a request here: https://github.com/TheDutchMC/SkinFixer/issues/new/choose. Your OS is '%s', make sure you mention this in your request!", System.getProperty("os.name")));
				break saveLib;
			}
			
			Pair<File, File> pairedFile = saveLib(libName);
			if(pairedFile == null) {
				break saveLib;
			}
			
			File tmpDir = pairedFile.getA();
			File libTmpFile = pairedFile.getB();
			
			try {
				System.load(libTmpFile.getAbsolutePath());
			} catch(UnsatisfiedLinkError e) {
				SkinFixer.logWarn("Failed to load libskinfixer. Please open an issue at https://github.com/TheDutchMC/SkinFixer/issues and include your OS, architecture and SkinFixer version. Thanks!");
				SkinFixer.logWarn(Utils.getStackTrace(e));
			}
			
			SkinFixer.logInfo("libskinfixer loaded.");
			LIB_LOADED = true;
		}
	}
	
	@Nullable
	private static Pair<File, File> saveLib(String libName) {
		URL libUrl = LibWrapper.class.getResource(libName);
		File tmpDir;
		try {
			tmpDir = Files.createTempDirectory("libskinfixer").toFile();
		} catch (IOException e) {
			SkinFixer.logWarn("Failed to create temporary directory: " + e);
			SkinFixer.logWarn(Utils.getStackTrace(e));
			return null;
		}
		
		String[] libNameParts = libName.split(Pattern.quote("/"));
		File libTmpFile = new File(tmpDir, libNameParts[libNameParts.length -1]);
		
		try {
			InputStream is = libUrl.openStream();
			Files.copy(is, libTmpFile.toPath());
		} catch(IOException e) {
			tmpDir.delete();
			SkinFixer.logWarn("Failed to save dynamic library as temporay file: " + e);
			SkinFixer.logWarn(Utils.getStackTrace(e));
			return null;
		}
		
		libTmpFile.deleteOnExit();
		tmpDir.deleteOnExit();
		
		return new Pair<>(tmpDir, libTmpFile);
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

		String host, database, username, password;
		SqlSettings sqlSettings = configManifest.sqlSettings;
		if(sqlSettings != null) {
			host = (sqlSettings.host != null) ? sqlSettings.host : "";
			database = (sqlSettings.database != null) ? sqlSettings.database : "";
			username = (sqlSettings.username != null) ? sqlSettings.username : "";
			password = (sqlSettings.password != null) ? sqlSettings.password : "";	
		} else {
			host = database = username = password = "";
		}
		
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
