package dev.array21.skinfixer;

import java.util.*;

import dev.array21.bukkitreflectionlib.exceptions.ReflectException;
import dev.array21.skinfixer.reflect.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


import dev.array21.skinfixer.apis.SkinFixerApi;
import dev.array21.skinfixer.apis.gson.GetSkinResponse;
import dev.array21.skinfixer.language.LangHandler;
import dev.array21.skinfixer.storage.SkinData;
import dev.array21.skinfixer.util.Triple;
import net.md_5.bungee.api.ChatColor;

public class SkinChangeHandler {
	
	private final SkinFixer plugin;
	
	public SkinChangeHandler(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	public void changeSkinJson(String skinUrl, UUID internalUuid, UUID externalUuid, boolean slim, boolean isPremium, boolean onLogin) {

		//Everything needs to be async, because the watchdog will kill the server because it takes too long
		new BukkitRunnable() {

			@Override
			public void run() {				
				Player player = Bukkit.getPlayer(internalUuid);
				
				if(!(onLogin && SkinChangeHandler.this.plugin.getConfigManifest().disableSkinApplyOnLoginMessage)) { 
					player.sendMessage(ChatColor.GOLD + LangHandler.model.skinFetching);
				}
				
				//Fetch the skin from Mineskin.org's API
				Triple<Boolean, GetSkinResponse, String> apiResponse;
				if(isPremium) {
					apiResponse = new SkinFixerApi().getSkinOfPremiumPlayer(externalUuid.toString());
				} else {
					apiResponse = new SkinFixerApi().getSkin(skinUrl, slim);
				}
				
				if(!apiResponse.getA()) {
					player.sendMessage(ChatColor.RED + LangHandler.model.skinApplyFailed.replaceAll("%ERROR%", ChatColor.GRAY + apiResponse.getC() + ChatColor.RED));
					return;
				}
				
				GetSkinResponse skinResponse = apiResponse.getB();
				changeSkin(skinResponse.value, skinResponse.signature, internalUuid, slim, onLogin);
			}
		}.runTaskAsynchronously(this.plugin);
	}
	
	public void changeSkinFromObject(SkinObject skin, boolean onLogin) {
		changeSkin(skin.getValue(), skin.getSignature(), skin.getOwner(), skin.getSlim(), onLogin);
	}
	
	public void changeSkinFromUuid(UUID mojangUuid, UUID localPlayerUuid, boolean slim) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Player p = Bukkit.getPlayer(localPlayerUuid);
				Triple<Boolean, GetSkinResponse, String> apiResponse = new SkinFixerApi().getSkinOfPremiumPlayer(mojangUuid.toString());
				if(!apiResponse.getA()) {
					p.sendMessage(ChatColor.RED + LangHandler.model.skinApplyFailed.replaceAll("%ERROR%", ChatColor.GRAY + apiResponse.getC() + ChatColor.RED));
					return;
				}
				
				GetSkinResponse skinResponse = apiResponse.getB();
				changeSkin(skinResponse.value, skinResponse.signature, localPlayerUuid, slim, false);
			}
		}.runTaskAsynchronously(this.plugin);
	}
	
	private void changeSkin(String skinValue, String skinSignature, UUID caller, boolean slim, boolean onLogin) {
		Player player = Bukkit.getPlayer(caller);
		SkinData skinData = this.plugin.getLibWrapper().getSkinProfile(caller);
		
		if(skinData != null) {
			SkinObject skin = skinData.into();
			if(slim) {
				skin.setSlim(true);
			}
			skin.updateSkin(skinValue, skinSignature);
		} else {
			SkinObject skin = new SkinObject(caller, skinValue, skinSignature);
			if(slim) {
				skin.setSlim(true);
			}			
		}
		
		this.plugin.getLibWrapper().setSkinProfile(new SkinData(caller, skinValue, skinSignature));
		
		if(!(onLogin && SkinChangeHandler.this.plugin.getConfigManifest().disableSkinApplyOnLoginMessage)) { 
			player.sendMessage(ChatColor.GOLD + LangHandler.model.skinApplying);
		}
		
		applySkin(player, skinValue, skinSignature);
		reloadPlayer(player);
		
		if(!(onLogin && SkinChangeHandler.this.plugin.getConfigManifest().disableSkinApplyOnLoginMessage)) { 
			//Inform the player that we're done
			player.sendMessage(ChatColor.GOLD + LangHandler.model.skinApplied);
		}
	}

	private void applySkin(Player player, String skinValue, String skinSignature) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				try {
					PlayerManager.setSkin(player, skinValue, skinSignature);
					PlayerManager.reloadPlayer(SkinChangeHandler.this.plugin, player);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}.runTask(this.plugin);
	}
	
	/**
	 * Reload the Player so the Player's new skin shows for other players and the Player itself
	 * @param player The Player to reload
	 */
	private void reloadPlayer(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					PlayerManager.reloadPlayer(SkinChangeHandler.this.plugin, player);
				} catch (ReflectException e) {
					SkinFixer.logWarn("Unable to reload the Player due to a reflection exception. Is your Minecraft versions supported? Please open a bug report here: https://github.com/TobiasDeBruijn/SkinFixer/issues and attach the following stack trace:");
					e.printStackTrace();
				}
			}
		}.runTask(this.plugin);
	}
}
