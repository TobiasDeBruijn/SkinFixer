package nl.thedutchmc.SkinFixer.minecraftEvents;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.SkinObject;
import nl.thedutchmc.SkinFixer.changeSkin.SkinChangeOrchestrator;
import nl.thedutchmc.SkinFixer.files.StorageHandler;

public class PlayerJoinEventListener implements Listener {

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		
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
}