package nl.thedutchmc.SkinFixer.changeSkin.changeGameProfile;

import java.util.UUID;

import org.bukkit.entity.Player;

public interface ChangeGameProfile {

	public void changeProfile(UUID uuid, String skinValue, String skinSignature);
	
	public void reloadSkinForSelf(Player p);
}
