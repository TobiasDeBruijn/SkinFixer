package dev.array21.skinfixer;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.hash.Hashing;

import dev.array21.skinfixer.apis.SkinFixerApi;
import dev.array21.skinfixer.gson.GetSkinResponse;
import dev.array21.skinfixer.language.LangHandler;
import dev.array21.skinfixer.storage.SkinData;
import dev.array21.skinfixer.util.ReflectionUtil;
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
				if(isPremium ) {
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
	
	private void changeSkin(String skinValue, String skinSignature, UUID caller, boolean slim, boolean onLogin) {
		Player player = Bukkit.getPlayer(caller);
		SkinData skinData = this.plugin.getStorageHandler().getManifest().getForPlayer(caller);
		
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
		
		this.plugin.getStorageHandler().updateSkinData(caller, skinValue, skinSignature);
		
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
	
	@SuppressWarnings("deprecation")
	private void applySkin(Player player, String skinValue, String skinSignature) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				try {
					Class<?> craftPlayerClass = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
					Object entityPlayer = ReflectionUtil.invokeMethod(craftPlayerClass, player, "getHandle");
					
					//Get the GameProfile and the PropertyMap inside the Profile
					Class<?> entityHumanClass;
					if(ReflectionUtil.isUseNewSpigotPackaging()) {
						entityHumanClass = ReflectionUtil.getMinecraftClass("world.entity.player.EntityHuman");
					} else {
						entityHumanClass = ReflectionUtil.getNmsClass("EntityHuman");
					}
					
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
					
					//Get the Enum constants REMOVE_PLAYER and ADD_PLAYER
					Class<?> packetPlayOutPlayerInfoClass;
					if(ReflectionUtil.isUseNewSpigotPackaging()) {
						packetPlayOutPlayerInfoClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo");
					} else {
						packetPlayOutPlayerInfoClass = ReflectionUtil.getNmsClass("PacketPlayOutPlayerInfo");
					}
					Object removePlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "REMOVE_PLAYER");
					Object addPlayerEnumConstant = ReflectionUtil.getEnum(packetPlayOutPlayerInfoClass, "EnumPlayerInfoAction", "ADD_PLAYER");
									    
				    //Create an Array of EntityPlayer with size = 1 and add our player to it
				    Object entityPlayerArr = Array.newInstance(entityPlayer.getClass(), 1);
				    Array.set(entityPlayerArr, 0, entityPlayer);
				    
					//Create two PacketPlayOutPlayerInfo packets, one for removing the player and one for re-adding the player
				    Object packetPlayOutPlayerInfoRemovePlayer;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
				    	
				    	packetPlayOutPlayerInfoRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass, 
					    		new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
					    		new Object[] { removePlayerEnumConstant, entityPlayerArr });
				    } else {
				    	packetPlayOutPlayerInfoRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass, removePlayerEnumConstant, entityPlayerArr);
				    }
				    
					Object packetPlayOutPlayerInfoAddPlayer;
					if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

						packetPlayOutPlayerInfoAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass,
								new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
								new Object[] { removePlayerEnumConstant, entityPlayerArr }
								);
					} else {
						packetPlayOutPlayerInfoAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfoClass, addPlayerEnumConstant, entityPlayerArr);
					}
					
					//Get the Player's connection and the generic Packet class
					Object playerConnection;
					if(ReflectionUtil.isUseNewSpigotPackaging()) {
						playerConnection = ReflectionUtil.getObject(entityPlayer, "b");
					} else {
						playerConnection = ReflectionUtil.getObject(entityPlayer, "playerConnection");
					}
					
					Class<?> packetClass;
					if(ReflectionUtil.isUseNewSpigotPackaging()) {
						packetClass = ReflectionUtil.getMinecraftClass("network.protocol.Packet");
					} else {
						packetClass = ReflectionUtil.getNmsClass("Packet");
					}

				    //Send the two Packets
				    ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoRemovePlayer });
				    ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPlayerInfoAddPlayer });
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
	@SuppressWarnings("deprecation")
	private void reloadPlayer(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Location playerLocation = player.getLocation().clone();
				
			    //Reload the player for all online players
				//This is so all other Players can see the new skin
			    Bukkit.getOnlinePlayers().forEach(p -> {
			    	p.hidePlayer(SkinChangeHandler.this.plugin, player);
			    	p.showPlayer(SkinChangeHandler.this.plugin, player);
			    });
				
			    //Remove and re-add the Player to the world
			    //This is so the Player who applied the skin can see their new skin
			    try {
			    	//Get the CraftPlayer class and turn our regular Player into an EntityPlayer
				    Class<?> craftPlayerClass = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
				    Object entityPlayer = ReflectionUtil.invokeMethod(craftPlayerClass, player, "getHandle");

				    //Get the CraftWorld class and turn our regular World into a WorldServer
				    Class<?> craftWorldClass = ReflectionUtil.getBukkitClass("CraftWorld");
				    Object worldServer = ReflectionUtil.invokeMethod(craftWorldClass, playerLocation.getWorld(), "getHandle");
				    				    				    				    			
				    //Get the PlayerInteractManagar,
				    //From that get the Gamemode of the player as an EnumGamemode and the numerical ID for that
				    Object playerIntManager;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	playerIntManager = ReflectionUtil.getObject(entityPlayer, "d");
				    } else {
				    	playerIntManager = ReflectionUtil.getObject(entityPlayer, "playerInteractManager");
				    }
				    
				    Enum<?> enumGamemode = (Enum<?>) ReflectionUtil.invokeMethod(playerIntManager, "getGameMode");
				    int gamemodeId = (int) ReflectionUtil.invokeMethod(enumGamemode, "getId");
				    
				    //Get the World's seed, and hash it with sha256
				    Object seed = ReflectionUtil.invokeMethod(playerLocation.getWorld(), "getSeed");
				    long seedHashed = Hashing.sha256().hashString(seed.toString(), StandardCharsets.UTF_8).asLong();
				    
				    //Get the EnumGamemode value from the gamemode ID.
				    //We can't use ReflectionUtil to invoke the method because that convert
				    //the primitive int to it's wrapper Integer.
			    	Method getGamemodeByIdMethod = ReflectionUtil.getMethod(enumGamemode.getClass(), "getById", int.class);
			    	Object gamemodeEnumConst = getGamemodeByIdMethod.invoke(null, gamemodeId);
				    
			    	//PacketPlayOutRespawn Class
				    Class<?> playPacketOutRespawnClass;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	playPacketOutRespawnClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutRespawn");
				    } else {
				    	playPacketOutRespawnClass = ReflectionUtil.getNmsClass("PacketPlayOutRespawn");
				    }
				    

				    //Instantiate the PacketPlayOutRespawn packet,
				    //Different Minecraft versions have slightly different constructors, so we have multiple
				    Object packetPlayOutRespawn;
				    try {
				    	// 1.16.1
				    	
				    	//TypeKey and DimensionKey
				    	Object typeKey = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "getTypeKey");
				    	Object dimensionKey = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "getDimensionKey");
				    	
				    	//Instantiate the Packet
				    	packetPlayOutRespawn = ReflectionUtil.invokeConstructor(playPacketOutRespawnClass,
			    			new Class<?>[] {
			    				typeKey.getClass(),
			    				dimensionKey.getClass(),
			    				long.class,
			    				enumGamemode.getClass(),
			    				enumGamemode.getClass(),
			    				boolean.class,
			    				boolean.class,
			    				boolean.class
			    			}, new Object[] {
				    			typeKey,
				    			dimensionKey,
				    			seedHashed,
				    			gamemodeEnumConst,
				    			gamemodeEnumConst,
				    			
				    			//isDebugWorld is in the nms class World, which WorldServer extends
				    			ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "isDebugWorld"),
				    			
				    			//isFlatWorld is in the nms class WorldServer for some reason (I expected it in the nms class World)
				    			ReflectionUtil.invokeMethod(worldServer, "isFlatWorld"),
				    			true
			    			});
				    	
				    } catch(Exception ignored) {
				    	// 1.16.2+

				    	// DimensionManager and DimensionKey
                        Object dimensionManager = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "getDimensionManager");
                        Object dimensionKey = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "getDimensionKey");
                        
    				    /* PacketPlayOutRespawn: 
    				     * Mojang's variable names to their 'I know what this is'-name 
    				     * a: (DimensionManager) DimensionManager
    				     * b: (ResourceKey) ResourceKey<World>
    				     * c: (long) Sha256 of the seed
    				     * d: (GameType) PlayerGameType
    				     * e: (GameType) previousPlayerGameType
    				     * f: (boolean) isDebug
    				     * g: (boolean) isFlat
    				     * h: (boolean) keepAllPlayerData 
    				     * */
				    	packetPlayOutRespawn = ReflectionUtil.invokeConstructor(playPacketOutRespawnClass,
			    			new Class<?>[] {
			    				dimensionManager.getClass(), 
			    				dimensionKey.getClass(), 
			    				long.class, 
			    				enumGamemode.getClass(), 
			    				enumGamemode.getClass(), 
			    				boolean.class, 
			    				boolean.class, 
			    				boolean.class
			    			}, new Object[] {
				    			dimensionManager,
				    			dimensionKey,
				    			seedHashed,
				    			gamemodeEnumConst,
				    			gamemodeEnumConst,
				    			
				    			//isDebugWorld is in the nms class World, which WorldServer extends
				    			ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "isDebugWorld"),
				    			
				    			//isFlatWorld is in the nms class WorldServer for some reason (I expected it in the nms class World)
				    			ReflectionUtil.invokeMethod(worldServer, "isFlatWorld"),
				    			true
				    		});
			    	}
				    
				    //PacketPlayOutPosition
				    Class<?> packetPlayOutPositionClass;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	packetPlayOutPositionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPosition");
				    } else {
				    	packetPlayOutPositionClass = ReflectionUtil.getNmsClass("PacketPlayOutPosition");
				    }
 
				    Object packetPlayOutPosition;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	packetPlayOutPosition = ReflectionUtil.invokeConstructor(packetPlayOutPositionClass, 
					    		new Class<?>[] { double.class, double.class, double.class, float.class, float.class, Set.class, int.class, boolean.class },
					    		new Object[] { playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(), playerLocation.getYaw(), playerLocation.getPitch(), new HashSet<Enum<?>>(), 0, false });
				    } else {
				    	packetPlayOutPosition = ReflectionUtil.invokeConstructor(packetPlayOutPositionClass, 
					    		new Class<?>[] { double.class, double.class, double.class, float.class, float.class, Set.class, int.class },
					    		new Object[] { playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(), playerLocation.getYaw(), playerLocation.getPitch(), new HashSet<Enum<?>>(), 0 });
				    }
				    
				    //PacketPlayOutHeldItem
				    Class<?> packetPlayOutHeldItemSlotClass;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	packetPlayOutHeldItemSlotClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutHeldItemSlot");
				    } else {
				    	packetPlayOutHeldItemSlotClass = ReflectionUtil.getNmsClass("PacketPlayOutHeldItemSlot");
				    }
				   
				    Object packetPlayOutHeldItemSlot = ReflectionUtil.invokeConstructor(packetPlayOutHeldItemSlotClass, 
				    		new Class<?>[] { int.class },
				    		new Object[] { player.getInventory().getHeldItemSlot() });
				    
				    //Get the EntityPlayers' connection
					//Get the Player's connection and the generic Packet class
					Object playerConnection;
					if(ReflectionUtil.isUseNewSpigotPackaging()) {
						playerConnection = ReflectionUtil.getObject(entityPlayer, "b");
					} else {
						playerConnection = ReflectionUtil.getObject(entityPlayer, "playerConnection");
					}
				    
				    //Get the Enum constants for REMOVE_PLAYER and ADD_PLAYER
				    Class<?> packetPlayOutPlayerInfo; 
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	packetPlayOutPlayerInfo = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo");
				    } else {
					    packetPlayOutPlayerInfo = ReflectionUtil.getNmsClass("PacketPlayOutPlayerInfo");
				    }
				    
				    Object removePlayerEnumConst = ReflectionUtil.getEnum(packetPlayOutPlayerInfo, "EnumPlayerInfoAction", "REMOVE_PLAYER");
				    Object addPlayerEnumConst = ReflectionUtil.getEnum(packetPlayOutPlayerInfo, "EnumPlayerInfoAction", "ADD_PLAYER");
				    				    
				    //Create an Array of EntityPlayer with size = 1 and add our player to it
				    Object entityPlayerArr = Array.newInstance(entityPlayer.getClass(), 1);
				    Array.set(entityPlayerArr, 0, entityPlayer);
				    
				    //Construct a PacketPlayOutPlayerInfo with intention REMOVE_PLAYER
				    Object packetPlayOutRemovePlayer;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

				    	packetPlayOutRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfo, 
					    		new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() }, 
					    		new Object[] { removePlayerEnumConst, entityPlayerArr });
				    } else {
				    	packetPlayOutRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfo, 
					    		new Class<?>[] { removePlayerEnumConst.getClass(), entityPlayerArr.getClass() }, 
					    		new Object[] { removePlayerEnumConst, entityPlayerArr });
				    }
				    
				    //Construct a PacketPlayOutPlayerInfo with intention ADD_PLAYER
				    Object packetPlayOutAddPlayer;
				    if(ReflectionUtil.isUseNewSpigotPackaging()) {
				    	Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
				    	packetPlayOutAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfo, 
					    		new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() }, 
					    		new Object[] { addPlayerEnumConst, entityPlayerArr });
				    } else {
				    	packetPlayOutAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfo, 
					    		new Class<?>[] { addPlayerEnumConst.getClass(), entityPlayerArr.getClass() },
					    		new Object[] { addPlayerEnumConst, entityPlayerArr });
				    }
				    
				    //Get the generic Packet class
					Class<?> packetClass;
					if(ReflectionUtil.isUseNewSpigotPackaging()) {
						packetClass = ReflectionUtil.getMinecraftClass("network.protocol.Packet");
					} else {
						packetClass = ReflectionUtil.getNmsClass("Packet");
					}				    
				    //Send both the PacketPlayOutPlayerInfo packets
				    ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutRemovePlayer });
				    ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutAddPlayer });
				    
				    //Send the PacketPlayOutRespawn packet
				    ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutRespawn });
				    
				    //Update the Player's abilities
				    ReflectionUtil.invokeMethod(entityPlayer, "updateAbilities");
				    
				    //Send both the PacketPlayOutPosition and PacketPlayOutHeldItem packets
				    ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] {packetPlayOutPosition});
				    ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] {packetPlayOutHeldItemSlot});
				    
				    //Update the Player's healthbar and inventory
		            ReflectionUtil.invokeMethod(player, "updateScaledHealth");
		            ReflectionUtil.invokeMethod(player, "updateInventory");
		            ReflectionUtil.invokeMethod(entityPlayer, "triggerHealthUpdate");

		            //If the Player is OP, we have to toggle it off and back on really quickly for it to work
		            if(player.isOp()) {
		            	Bukkit.getScheduler().runTask(SkinChangeHandler.this.plugin, () -> {
		            		player.setOp(false);
		            		player.setOp(true);
		            	});
		            }
			    } catch(Exception e) {
			    	e.printStackTrace();
			    }
			}
		}.runTask(this.plugin);
	}
}
