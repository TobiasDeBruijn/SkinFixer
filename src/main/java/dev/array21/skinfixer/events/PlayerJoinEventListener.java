package dev.array21.skinfixer.events;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import dev.array21.skinfixer.SkinChangeHandler;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.apis.SkinFixerApi;
import dev.array21.skinfixer.storage.SkinData;
import dev.array21.skinfixer.util.Triple;
import dev.array21.skinfixer.util.Utils;

public class PlayerJoinEventListener implements Listener {

	private SkinFixer plugin;
	
	public PlayerJoinEventListener(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				SkinData sd = PlayerJoinEventListener.this.plugin.getLibWrapper().getSkinProfile(event.getPlayer().getUniqueId());
				if(sd != null) {
					new BukkitRunnable() {
						
						@Override
						public void run() {
							new SkinChangeHandler(PlayerJoinEventListener.this.plugin).changeSkinFromObject(sd.into(), true);
						}
					}.runTaskLater(PlayerJoinEventListener.this.plugin, 20L);
					
					return;
				}
								
				Triple<Boolean, String, String> mojangApiResponse = new SkinFixerApi().getUuid(event.getPlayer().getName());
				if(!mojangApiResponse.getA()) {
					SkinFixer.logWarn("Something went wrong fetching the UUID from Mojang.");
				} else if(mojangApiResponse.getB() != null) {
					String uuidDashedStr = Utils.insertDashUUID(mojangApiResponse.getB());
					new SkinChangeHandler(PlayerJoinEventListener.this.plugin).changeSkinJson(null, event.getPlayer().getUniqueId(), UUID.fromString(uuidDashedStr), false, true, true);
					return;
				}
			}
		}.runTaskAsynchronously(this.plugin);
	}
}