package nl.thedutchmc.SkinFixer.minecraftEvents;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.SkinObject;
import nl.thedutchmc.SkinFixer.changeSkin.CheckUserAgainstMojang;
import nl.thedutchmc.SkinFixer.changeSkin.SkinChangeOrchestrator;
import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;

public class PlayerJoinEventListener implements Listener {

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				String uuidAsString = CheckUserAgainstMojang.premiumUser(event.getPlayer().getName());
				if(uuidAsString != null) {
					
					SkinChangeOrchestrator.changeSkinJson(null, event.getPlayer().getUniqueId(), UUID.fromString(uuidAsString), false, true);
					return;
				}

				if(!StorageHandler.skins.containsKey(event.getPlayer().getUniqueId())) {
					return;
				}
				
				SkinObject skin = StorageHandler.skins.get(event.getPlayer().getUniqueId());
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						SkinChangeOrchestrator.changeSkinFromObject(skin);
					}
				}.runTaskLater(SkinFixer.INSTANCE, 5L);
			}
		}.runTaskAsynchronously(SkinFixer.INSTANCE);
	}
}