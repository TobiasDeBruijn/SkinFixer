package nl.thedutchmc.SkinFixer.changeSkin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.SkinObject;
import nl.thedutchmc.SkinFixer.StorageHandler;
import nl.thedutchmc.SkinFixer.changeSkin.changeGameProfile.ChangeGameProfile;
import nl.thedutchmc.SkinFixer.changeSkin.changeGameProfile.ChangeGameProfile_1_16_r1;

public class SkinChangeOrchestrator {

	static ChangeGameProfile changeGameProfile;
	
	public static void changeSkin(String skinUrl, UUID caller) {

		//Everything needs to be async, because the watchdog will kill the server because it takes too long
		new BukkitRunnable() {
						
			Player p = Bukkit.getPlayer(caller);
			String value, signature;
			
			public void run() {

				p.sendMessage(ChatColor.GOLD + "Fetching skin value and signature...");
				
				//Fetch the skin from Mineskin.org's API
				String skinJson = GetSkin.getSkin(skinUrl);
								
				//Get the skin texture value, and the skin texture signature
				JSONTokener tokener = new JSONTokener(skinJson);
				
				//Descent to the Texture object
				JSONObject full = (JSONObject) tokener.nextValue();
				JSONObject data = (JSONObject) full.get("data");
				JSONObject texture = (JSONObject) data.get("texture");
				
				//Grab the value and signature
				value = (String) texture.get("value");
				signature = (String) texture.get("signature");
				
				//Store the skin to the storage file, so it can be reapplied when they join.
				if(StorageHandler.skins.containsKey(caller)) {
					SkinObject skin = StorageHandler.skins.get(caller);
					skin.updateSkin(value, signature);
				} else {
					SkinObject skin = new SkinObject(caller, value, signature);
					StorageHandler.skins.put(caller, skin);
				}
				
				p.sendMessage(ChatColor.GOLD + "Applying skin...");
				
				//NMS is version dependant. So we need to set the correct class to use.
				switch(SkinFixer.NMS_VERSION) {
				case "v1_16_R1": changeGameProfile = new ChangeGameProfile_1_16_r1(); break;
		
				default:
					//We dont support the version that the user is running, so we inform them of this.
					//Calls to the Bukkit API may only be sync, so it's inside a BukkitRunnable
					new BukkitRunnable() {
						@Override
						public void run() {
							Player p = Bukkit.getPlayer(caller);
							p.sendMessage(ChatColor.RED + "This server is using a Minecraft version that is not supported by SkinFixer!");
							p.sendMessage(ChatColor.RED + "You are running NMS version " + SkinFixer.NMS_VERSION);
						}
					}.runTask(SkinFixer.INSTANCE);
				}
				
				//Change the skins on the GameProfile. Needs to be sync
				new BukkitRunnable() {
					
					@Override
					public void run() {
						Player p = Bukkit.getPlayer(caller);
						changeGameProfile.changeProfile(p.getUniqueId(), value, signature);
					}
				}.runTask(SkinFixer.INSTANCE);
				
				//Inform the player that we're done
				p.sendMessage(ChatColor.GOLD + "Done.");
			}
		}.runTaskAsynchronously(SkinFixer.INSTANCE);

	}
}
