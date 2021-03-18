package nl.thedutchmc.SkinFixer.minecraftevents;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.SkinObject;
import nl.thedutchmc.SkinFixer.apis.MojangApi;
import nl.thedutchmc.SkinFixer.changeSkin.SkinChangeHandler;
import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;
import nl.thedutchmc.SkinFixer.gson.MojangAuthResponse;
import nl.thedutchmc.SkinFixer.util.Triple;
import nl.thedutchmc.SkinFixer.util.Utils;

public class PlayerJoinEventListener implements Listener {

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				Triple<Boolean, MojangAuthResponse, String> mojangApiResponse = new MojangApi().getUuidFromMojang(event.getPlayer().getName());
				
				if(!mojangApiResponse.getA()) {
					SkinFixer.logWarn("Something went wrong fetching the UUID from Mojang.");
				} else if(mojangApiResponse.getB() != null) {
					String uuidDashedStr = Utils.insertDashUUID(mojangApiResponse.getB().getUuid());
					SkinChangeHandler.changeSkinJson(null, event.getPlayer().getUniqueId(), UUID.fromString(uuidDashedStr), false, true);
					return;
				}

				if(!StorageHandler.skins.containsKey(event.getPlayer().getUniqueId())) {
					return;
				}
				
				SkinObject skin = StorageHandler.skins.get(event.getPlayer().getUniqueId());
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						SkinChangeHandler.changeSkinFromObject(skin);
					}
				}.runTaskLater(SkinFixer.INSTANCE, 5L);
			}
		}.runTaskAsynchronously(SkinFixer.INSTANCE);
	}
}