package nl.thedutchmc.SkinFixer.changeSkin.changeGameProfile;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

/* It is normal for all net.minecraft.server and org.bukkit.craftbukkit imports
 * to error when you are not working on that version.
 * Gradle cannot handle multiple versions
 */
public class ChangeGameProfile_1_16_r1  {

	public static void changeProfile(UUID uuid, String skinValue, String skinSignature) {
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
	    
	    //Reload the skin for the player itself
	    reloadSkinForSelf(player);
	}
	
    public static void reloadSkinForSelf(Player player) {
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        final PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ep);
        final PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ep);
        
        ep.playerConnection.sendPacket(removeInfo);
        ep.playerConnection.sendPacket(addInfo);
    }
}
