package dev.array21.skinfixer.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.util.Utils;

public class StorageHandler {

	private SkinFixer plugin;
	private StorageManifest manifest;
	
	private static final Gson GSON = new Gson();
	
	public StorageHandler(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	public StorageManifest read() {
		File storageFile = new File(this.plugin.getDataFolder(), "storage-v2.json");
		if(!storageFile.exists()) {
			storageFile.getParentFile().mkdirs();
		
			StorageManifest blankManifest = new StorageManifest();
			blankManifest.skinData = new SkinData[0];
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(storageFile));
				bw.write(GSON.toJson(blankManifest));
				bw.flush();
				bw.close();
			} catch(IOException e) {
				SkinFixer.logWarn(String.format("Failed to create empty storage file: %s", e.getMessage()));
				SkinFixer.logWarn(Utils.getStackTrace(e));
				Bukkit.getPluginManager().disablePlugin(this.plugin);
				return null;
			}
			
			this.manifest = blankManifest;
			return blankManifest;
		}

		StorageManifest manifest;
		try {
			FileReader fr = new FileReader(storageFile);
			manifest = GSON.fromJson(fr, StorageManifest.class);
		} catch(IOException e) {
			SkinFixer.logWarn(String.format("Failed to load storage file: %s", e.getMessage()));
			SkinFixer.logWarn(Utils.getStackTrace(e));
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return null;
		}
		
		this.manifest = manifest;
		return manifest;
	}
	
	public void updateSkinData(UUID player, String value, String signature) {
		SkinData skinData = new SkinData();
		skinData.playerUuid = player.toString();
		skinData.value = value;
		skinData.signature = signature;
		
		this.manifest.addSkinData(skinData);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				File storageFile = new File(StorageHandler.this.plugin.getDataFolder(), "storage-v2.json");
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(storageFile));
					bw.write(GSON.toJson(StorageHandler.this.manifest));
					bw.flush();
					bw.close();
				} catch(IOException e) {
					SkinFixer.logWarn(String.format("Failed to update storage file: %s", e.getMessage()));
					SkinFixer.logWarn(Utils.getStackTrace(e));
				}
			}
		}.runTaskAsynchronously(this.plugin);
	}
	
	public StorageManifest getManifest() {
		return this.manifest;
	}
}
