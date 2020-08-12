package nl.thedutchmc.SkinFixer.changeSkin.changeGameProfile;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import nl.thedutchmc.SkinFixer.SkinFixer;

public class ChangeGameProfile_1_16_r1 implements ChangeGameProfile {

	@Override
	public void changeProfile(UUID uuid, String skinValue, String skinSignature) {
		Player player = Bukkit.getPlayer(uuid);
		
		//Fetch the EntityPlayer and their GameProfile
	    EntityPlayer ep = ((CraftPlayer)player).getHandle();
	    GameProfile gp = ep.getProfile();
	    
	    //Get the skin texture property
	    PropertyMap pm = gp.getProperties();
	    	
	    //Check if the propertyMap contains a texture value, if so, remove it.
	    if(pm.containsKey("textures")) {
		    Property property = pm.get("textures").iterator().next();
		    pm.remove("textures", property);
	    }

	    //Remove the old texture, and set the new one.
	    pm.put("textures", new Property("textures", skinValue, skinSignature));
	    
	    //Reload the player for all online players
	    for(Player p : Bukkit.getOnlinePlayers()) {
	    	p.hidePlayer(SkinFixer.INSTANCE, player);
	    	p.showPlayer(SkinFixer.INSTANCE, player);
	    }
	    
	    //Reload the skin for the player itself
	    reloadSkinForSelf(player);
	}
	
	@Override
    public void reloadSkinForSelf(Player player) {
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        final PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ep);
        final PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ep);
        final Location loc = player.getLocation().clone();
        
        ep.playerConnection.sendPacket(removeInfo);
        ep.playerConnection.sendPacket(addInfo);
        
        System.out.println(loc.getWorld());
        
        World teleportToWorld = null;
        for(World w : Bukkit.getWorlds()) {
        	if(!w.equals(loc.getWorld())) teleportToWorld = w;
        }
                
        player.teleport(new Location(teleportToWorld, 0, 1, 0));
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(loc);                       
                player.updateInventory();
            }
        }.runTaskLater(SkinFixer.INSTANCE, 5L);
    }
}
