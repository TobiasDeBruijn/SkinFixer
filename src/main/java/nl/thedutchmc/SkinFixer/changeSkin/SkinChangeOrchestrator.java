package nl.thedutchmc.SkinFixer.changeSkin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.SkinObject;
import nl.thedutchmc.SkinFixer.changeSkin.changeGameProfile.*;
import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;

public class SkinChangeOrchestrator {
	
	public static void changeSkinJson(String skinUrl, UUID internalUuid, UUID externalUuid, boolean slim, boolean isPremium) {

		//Everything needs to be async, because the watchdog will kill the server because it takes too long
		new BukkitRunnable() {

			@Override
			public void run() {
				
				Player player = Bukkit.getPlayer(internalUuid);
				
				String value, signature;
				
				player.sendMessage(ChatColor.GOLD + "Fetching skin value and signature...");
				
				//Fetch the skin from Mineskin.org's API
				String skinJson = null;
				if(isPremium ) {
					skinJson = GetSkin.getSkinOfValidPlayer(externalUuid.toString());
				} else {
					skinJson = GetSkin.getSkin(skinUrl, slim);
				}
								
				//Get the skin texture value, and the skin texture signature
				JSONTokener tokener = new JSONTokener(skinJson);
								
				//Descent to the Texture object
				JSONObject full = (JSONObject) tokener.nextValue();
				JSONObject data = (JSONObject) full.get("data");
				JSONObject texture = (JSONObject) data.get("texture");
				
				//Grab the value and signature
				value = (String) texture.get("value");
				signature = (String) texture.get("signature");
				
				changeSkin(value, signature, internalUuid, slim);
			}
		}.runTaskAsynchronously(SkinFixer.INSTANCE);
	}
	
	public static void changeSkinFromObject(SkinObject skin) {
		new BukkitRunnable() {
			@Override
			public void run() {
				changeSkin(skin.getValue(), skin.getSignature(), skin.getOwner(), skin.getSlim());
			}
		}.runTaskAsynchronously(SkinFixer.INSTANCE);
	}
	
	private static void changeSkin(String skinValue, String skinSignature, UUID caller, boolean slim ) {
		Player player = Bukkit.getPlayer(caller);
		
		//Store the skin to the storage file, so it can be reapplied when they join.
		if(StorageHandler.skins.containsKey(caller)) {
			SkinObject skin = StorageHandler.skins.get(caller);
			if(slim) skin.setSlim(true);
			skin.updateSkin(skinValue, skinSignature);
		} else {
			SkinObject skin = new SkinObject(caller, skinValue, skinSignature);
			if(slim) skin.setSlim(true);
			StorageHandler.skins.put(caller, skin);
		}
		
		player.sendMessage(ChatColor.GOLD + "Applying skin...");
		
		new BukkitRunnable() {
			@Override
			public void run() {
				//NMS is version dependant. So we need to set the correct class to use.
				switch(SkinFixer.NMS_VERSION) {
				case "v1_16_R1": ChangeGameProfile_1_16_r1.changeProfile(player.getUniqueId(), skinValue, skinSignature); break;
				case "v1_16_R2": ChangeGameProfile_1_16_r2.changeProfile(player.getUniqueId(), skinValue, skinSignature); break;
				case "v1_16_R3": ChangeGameProfile_1_16_r3.changeProfile(player.getUniqueId(), skinValue, skinSignature); break;
				default:
					//We dont support the version that the user is running, so we inform them of this.
					//Calls to the Bukkit API may only be sync, so it's inside a BukkitRunnable
					Player p = Bukkit.getPlayer(caller);
					p.sendMessage(ChatColor.RED + "This server is using a Minecraft version that is not supported by SkinFixer!");
					p.sendMessage(ChatColor.RED + "You are running NMS version " + SkinFixer.NMS_VERSION);
				}
			}
		}.runTask(SkinFixer.INSTANCE);
		
		reloadPlayer(player);
		
		//Inform the player that we're done
		player.sendMessage(ChatColor.GOLD + "Done.");
	}
	
	private static void reloadPlayer(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Location loc = player.getLocation().clone();
				
			    //Reload the player for all online players
			    for(Player p : Bukkit.getOnlinePlayers()) {
			    	p.hidePlayer(SkinFixer.INSTANCE, player);
			    	p.showPlayer(SkinFixer.INSTANCE, player);
			    }
				
		        World teleportToWorld = null;
		        for(World w : Bukkit.getWorlds()) {
		        	if(!w.equals(loc.getWorld())) teleportToWorld = w;
		        }
		                
		        player.teleport(new Location(teleportToWorld, 0, 255, 0));
		        new BukkitRunnable() {
		            @Override
		            public void run() {
		            	player.teleport(loc);                       
		            	player.updateInventory();
		            }
		        }.runTaskLater(SkinFixer.INSTANCE, 5L);
			}
		}.runTask(SkinFixer.INSTANCE);
	}
}
