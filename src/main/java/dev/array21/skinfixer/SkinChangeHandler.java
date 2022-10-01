package dev.array21.skinfixer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.PlayerManager;
import dev.array21.skinfixer.reflect.ReflectException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.hash.Hashing;

import dev.array21.skinfixer.apis.SkinFixerApi;
import dev.array21.skinfixer.apis.gson.GetSkinResponse;
import dev.array21.skinfixer.language.LangHandler;
import dev.array21.skinfixer.storage.SkinData;
import dev.array21.skinfixer.util.Triple;
import net.md_5.bungee.api.ChatColor;

public class SkinChangeHandler {
	
	private SkinFixer plugin;
	
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

	private void applySkin1_16(Player player, String skinValue, String skinSignature) throws Exception {
		Class<?> craftPlayerClass = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
		Object entityPlayer = ReflectionUtil.invokeMethod(craftPlayerClass, player, "getHandle");
		Class<?> entityHumanClass = ReflectionUtil.getNmsClass("EntityHuman");

		Object gameProfile = ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "getProfile");
		Object propertyMap = ReflectionUtil.invokeMethod(gameProfile, "getProperties");

		//Check if the PropertyMap contains the 'textures' property
		//If so remove it
		//The containsKey method is in the ForwardingMultimap class, which PropertyMap extends
		Class<?> forwardingMultimapClass = com.google.common.collect.ForwardingMultimap.class;
		Boolean containsKeyTextures = (Boolean) ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "containsKey", new Class<?>[] { Object.class }, new Object[] { "textures" });
		if(containsKeyTextures) {
			Object textures = ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "get", new Class<?>[] { Object.class }, new Object[] { "textures" });
			Object texturesIter = ReflectionUtil.invokeMethod(Collection.class, textures, "iterator");
			Object iterNext = ReflectionUtil.invokeMethod(texturesIter, "next");

			ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "remove", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", iterNext });
		}

		//Create a new 'textures' Property with the new skinValue and skinSignature
		//and put it in the PropertyMap
		Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
		Object newProperty = ReflectionUtil.invokeConstructor(propertyClass, "textures", skinValue, skinSignature);
		ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "put", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", newProperty });

		Class<?> packetPlayOutPlayerInfoClass = ReflectionUtil.getNmsClass("PacketPlayOutPlayerInfo");

		Object removePlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "REMOVE_PLAYER");
		Object addPlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "ADD_PLAYER");

		//Create an Array of EntityPlayer with size = 1 and add our player to it
		Object entityPlayerArr = Array.newInstance(entityPlayer.getClass(), 1);
		Array.set(entityPlayerArr, 0, entityPlayer);

		Object packetPlayOutPlayerInfoRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass, removePlayerEnumConstant, entityPlayerArr);
		Object packetPlayOutPlayerInfoAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass, addPlayerEnumConstant, entityPlayerArr);

		Object playerConnection = ReflectionUtil.getObject(entityPlayer, "playerConnection");
		Class<?> packetClass = ReflectionUtil.getNmsClass("Packet");

		//Send the two Packets
		ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoRemovePlayer });
		ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoAddPlayer });
	}

	private void applySkin1_17(Player player, String skinValue, String skinSignature) throws Exception {
		Class<?> craftPlayerClass = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
		Object entityPlayer = ReflectionUtil.invokeMethod(craftPlayerClass, player, "getHandle");

		Class<?> entityHumanClass = ReflectionUtil.getMinecraftClass("world.entity.player.EntityHuman");

		Object gameProfile = ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "getProfile");
		Object propertyMap = ReflectionUtil.invokeMethod(gameProfile, "getProperties");

		//Check if the PropertyMap contains the 'textures' property
		//If so remove it
		//The containsKey method is in the ForwardingMultimap class, which PropertyMap extends
		Class<?> forwardingMultimapClass = com.google.common.collect.ForwardingMultimap.class;
		Boolean containsKeyTextures = (Boolean) ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "containsKey", new Class<?>[] { Object.class }, new Object[] { "textures" });
		if(containsKeyTextures) {
			Object textures = ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "get", new Class<?>[] { Object.class }, new Object[] { "textures" });
			Object texturesIter = ReflectionUtil.invokeMethod(Collection.class, textures, "iterator");
			Object iterNext = ReflectionUtil.invokeMethod(texturesIter, "next");

			ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "remove", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", iterNext });
		}

		//Create a new 'textures' Property with the new skinValue and skinSignature
		//and put it in the PropertyMap
		Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
		Object newProperty = ReflectionUtil.invokeConstructor(propertyClass, "textures", skinValue, skinSignature);
		ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "put", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", newProperty });

		Class<?> packetPlayOutPlayerInfoClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo");
		Object removePlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "REMOVE_PLAYER");
		Object addPlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "ADD_PLAYER");

		//Create an Array of EntityPlayer with size = 1 and add our player to it
		Object entityPlayerArr = Array.newInstance(entityPlayer.getClass(), 1);
		Array.set(entityPlayerArr, 0, entityPlayer);

		Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

		Object packetPlayOutPlayerInfoRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass,
				new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
				new Object[] { removePlayerEnumConstant, entityPlayerArr });

		Object packetPlayOutPlayerInfoAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass,
				new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
				new Object[] { removePlayerEnumConstant, entityPlayerArr });

		Object playerConnection = ReflectionUtil.getObject(entityPlayer, "b");
		Class<?> packetClass = ReflectionUtil.getMinecraftClass("network.protocol.Packet");

		//Send the two Packets
		ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoRemovePlayer });
		ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoAddPlayer });
	}

	private void applySkinNew(Player player, String skinValue, String skinSignature) throws Exception {
		Class<?> craftPlayerClass = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
		Object entityPlayer = ReflectionUtil.invokeMethod(craftPlayerClass, player, "getHandle");

		Object gameProfile = getGameProfile(entityPlayer);
		Object propertyMap = ReflectionUtil.invokeMethod(gameProfile, "getProperties");

		//Check if the PropertyMap contains the 'textures' property
		//If so remove it
		//The containsKey method is in the ForwardingMultimap class, which PropertyMap extends
		Class<?> forwardingMultimapClass = com.google.common.collect.ForwardingMultimap.class;
		Boolean containsKeyTextures = (Boolean) ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "containsKey", new Class<?>[] { Object.class }, new Object[] { "textures" });
		if(containsKeyTextures) {
			Object textures = ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "get", new Class<?>[] { Object.class }, new Object[] { "textures" });
			Object texturesIter = ReflectionUtil.invokeMethod(Collection.class, textures, "iterator");
			Object iterNext = ReflectionUtil.invokeMethod(texturesIter, "next");

			ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "remove", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", iterNext });
		}

		//Create a new 'textures' Property with the new skinValue and skinSignature
		//and put it in the PropertyMap
		Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
		Object newProperty = ReflectionUtil.invokeConstructor(propertyClass, "textures", skinValue, skinSignature);
		ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "put", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", newProperty });

		Class<?> packetPlayOutPlayerInfoClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo");
		Object removePlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "REMOVE_PLAYER");
		Object addPlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "ADD_PLAYER");

		//Create an Array of EntityPlayer with size = 1 and add our player to it
		Object entityPlayerArr = Array.newInstance(entityPlayer.getClass(), 1);
		Array.set(entityPlayerArr, 0, entityPlayer);

		Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

		Object packetPlayOutPlayerInfoRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass,
				new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
				new Object[] { removePlayerEnumConstant, entityPlayerArr });

		Object packetPlayOutPlayerInfoAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass,
				new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
				new Object[] { removePlayerEnumConstant, entityPlayerArr });

		Object playerConnection = ReflectionUtil.getObject(entityPlayer, "b");
		Class<?> packetClass = ReflectionUtil.getMinecraftClass("network.protocol.Packet");

		//Send the two Packets
		ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoRemovePlayer });
		ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoAddPlayer });
	}

	private Object getGameProfile(Object entityPlayer) throws Exception {
		Class<?> entityHumanClass = ReflectionUtil.getMinecraftClass("world.entity.player.EntityHuman");

		return switch(ReflectionUtil.getMajorVersion()) {
			case 19 -> switch(ReflectionUtil.getMinorVersion()) {
				case 1, 2 -> ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "fy");
				default -> ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "fz");
			};
			case 18 -> switch(ReflectionUtil.getMinorVersion()) {
				case 2 -> ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "fq");
				default -> ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "fp");
			};
			case 16, 17 -> ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "getProfile");
			default -> throw new RuntimeException("Unsupported Minecraft version!");
		};
	}

	private void applySkin1_18(Player player, String skinValue, String skinSignature) throws Exception {
		Class<?> craftPlayerClass = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
		Object entityPlayer = ReflectionUtil.invokeMethod(craftPlayerClass, player, "getHandle");

		Class<?> entityHumanClass = ReflectionUtil.getMinecraftClass("world.entity.player.EntityHuman");

		Object gameProfile;
		if(ReflectionUtil.getMinorVersion() >= 2) {
			gameProfile = ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "fq");
		} else {
			gameProfile = ReflectionUtil.invokeMethod(entityHumanClass, entityPlayer, "fp");
		}

		Object propertyMap = ReflectionUtil.invokeMethod(gameProfile, "getProperties");

		//Check if the PropertyMap contains the 'textures' property
		//If so remove it
		//The containsKey method is in the ForwardingMultimap class, which PropertyMap extends
		Class<?> forwardingMultimapClass = com.google.common.collect.ForwardingMultimap.class;
		Boolean containsKeyTextures = (Boolean) ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "containsKey", new Class<?>[] { Object.class }, new Object[] { "textures" });
		if(containsKeyTextures) {
			Object textures = ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "get", new Class<?>[] { Object.class }, new Object[] { "textures" });
			Object texturesIter = ReflectionUtil.invokeMethod(Collection.class, textures, "iterator");
			Object iterNext = ReflectionUtil.invokeMethod(texturesIter, "next");

			ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "remove", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", iterNext });
		}

		//Create a new 'textures' Property with the new skinValue and skinSignature
		//and put it in the PropertyMap
		Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
		Object newProperty = ReflectionUtil.invokeConstructor(propertyClass, "textures", skinValue, skinSignature);
		ReflectionUtil.invokeMethod(forwardingMultimapClass, propertyMap, "put", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", newProperty });

		Class<?> packetPlayOutPlayerInfoClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo");
		Object removePlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "REMOVE_PLAYER");
		Object addPlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "ADD_PLAYER");

		//Create an Array of EntityPlayer with size = 1 and add our player to it
		Object entityPlayerArr = Array.newInstance(entityPlayer.getClass(), 1);
		Array.set(entityPlayerArr, 0, entityPlayer);

		Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

		Object packetPlayOutPlayerInfoRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass,
				new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
				new Object[] { removePlayerEnumConstant, entityPlayerArr });

		Object packetPlayOutPlayerInfoAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass,
				new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
				new Object[] { removePlayerEnumConstant, entityPlayerArr });

		Object playerConnection = ReflectionUtil.getObject(entityPlayer, "b");
		Class<?> packetClass = ReflectionUtil.getMinecraftClass("network.protocol.Packet");

		//Send the two Packets
		ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoRemovePlayer });
		ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoAddPlayer });
	}

	@SuppressWarnings("deprecation")
	private void applySkin(Player player, String skinValue, String skinSignature) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				try {
					if(!ReflectionUtil.isUseNewSpigotPackaging()) {
						applySkin1_16(player, skinValue, skinSignature);
						return;
					}

					switch(ReflectionUtil.getMajorVersion()) {
						case 17: applySkin1_17(player, skinValue, skinSignature); break;
						case 18: applySkin1_18(player, skinValue, skinSignature); break;
						default: applySkinNew(player, skinValue, skinSignature); break;
					}
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
