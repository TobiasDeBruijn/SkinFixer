package dev.array21.skinfixer;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
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
						default:
							applySkin1_18(player, skinValue, skinSignature); break;
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

					Enum<?> enumGamemode;
					int gamemodeId;
					if(ReflectionUtil.getMajorVersion() >= 18) {
						enumGamemode = (Enum<?>) ReflectionUtil.invokeMethod(playerIntManager, "b");
						gamemodeId = (int) ReflectionUtil.invokeMethod(enumGamemode, "a");
					} else {
						enumGamemode = (Enum<?>) ReflectionUtil.invokeMethod(playerIntManager, "getGameMode");
						gamemodeId = (int) ReflectionUtil.invokeMethod(enumGamemode, "getId");
					}
				    
				    //Get the World's seed, and hash it with sha256
				    Object seed = ReflectionUtil.invokeMethod(playerLocation.getWorld(), "getSeed");
				    long seedHashed = Hashing.sha256().hashString(seed.toString(), StandardCharsets.UTF_8).asLong();
				    
				    //Get the EnumGamemode value from the gamemode ID.
				    //We can't use ReflectionUtil to invoke the method because that convert
				    //the primitive int to it's wrapper Integer.
					Method getGamemodeByIdMethod;
					if(ReflectionUtil.getMajorVersion() >= 18) {
						getGamemodeByIdMethod = ReflectionUtil.getMethod(enumGamemode.getClass(), "a", int.class);
					} else {
						getGamemodeByIdMethod = ReflectionUtil.getMethod(enumGamemode.getClass(), "getById", int.class);
					}
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

						Object dimensionManager, dimensionKey;
						if(ReflectionUtil.getMajorVersion() >= 18) {
							dimensionManager = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "q_");
							dimensionKey = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "aa");
						} else {
							dimensionManager = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "getDimensionManager");
							dimensionKey = ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "getDimensionKey");
						}

						Class<?> argumentAClass;
						Object argumentAValue;
						if((ReflectionUtil.getMajorVersion() == 18 && ReflectionUtil.getMinorVersion() >= 2) || ReflectionUtil.getMajorVersion() > 18) {
							Class<?> holderClass = ReflectionUtil.getMinecraftClass("core.Holder");
							Object dimensionManagerHolder = ReflectionUtil.invokeMethod(holderClass, null, "a", new Class<?>[] { Object.class }, new Object[] { dimensionManager });

							argumentAClass = holderClass;
							argumentAValue = holderClass.cast(dimensionManagerHolder);
						} else {
							argumentAClass = dimensionManager.getClass();
							argumentAValue = dimensionManager;
						}

						boolean isDebugWorld;
						if(ReflectionUtil.getMajorVersion() >= 18) {
							isDebugWorld = (boolean) ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "ad");
						} else {
							isDebugWorld = (boolean) ReflectionUtil.invokeMethod(worldServer.getClass().getSuperclass(), worldServer, "isDebugWorld");
						}

						// I hate obfuscations
						boolean isFlatWorld;
						if((ReflectionUtil.getMajorVersion() == 18 && ReflectionUtil.getMinorVersion() >= 2) || ReflectionUtil.getMajorVersion() > 18) {
							isFlatWorld = (boolean) ReflectionUtil.invokeMethod(worldServer, "C");
						} else if(ReflectionUtil.getMajorVersion() == 18) {
							isFlatWorld = (boolean) ReflectionUtil.invokeMethod(worldServer, "D");
						} else {
							isFlatWorld = (boolean) ReflectionUtil.invokeMethod(worldServer, "isFlatWorld");
						}

						/* PacketPlayOutRespawn:
    				     * Mojang's variable names to their 'I know what this is'-name 
    				     * a: 1.18.1- (DimensionManager) DimensionManager ; 1.18.2+ (Holder<DimensionManager>)
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
								argumentAClass,
			    				dimensionKey.getClass(), 
			    				long.class, 
			    				enumGamemode.getClass(), 
			    				enumGamemode.getClass(), 
			    				boolean.class, 
			    				boolean.class, 
			    				boolean.class
			    			}, new Object[] {
								argumentAValue,
				    			dimensionKey,
				    			seedHashed,
				    			gamemodeEnumConst,
				    			gamemodeEnumConst,
								isDebugWorld,
								isFlatWorld,
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

					if(ReflectionUtil.getMajorVersion() >= 18) {
						ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutRemovePlayer }); // Send packet
						ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutAddPlayer });
						ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutRespawn });

						Class<?> packetPlaytOutExperienceClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutExperience");
						Object packetPlayOutExperience = ReflectionUtil.invokeConstructor(packetPlaytOutExperienceClass,
								new Class<?>[] { float.class, int.class, int.class },
								new Object[] { player.getExp(), player.getTotalExperience(), player.getLevel() });
						ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutExperience });

						ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutPosition });
						ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass }, new Object[] { packetPlayOutHeldItemSlot });
					} else {
						ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutRemovePlayer });
						ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutAddPlayer });
						ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] { packetPlayOutRespawn });
						ReflectionUtil.invokeMethod(entityPlayer, "updateAbilities");
						ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] {packetPlayOutPosition});
						ReflectionUtil.invokeMethod(playerConnection, "sendPacket", new Class<?>[] { packetClass }, new Object[] {packetPlayOutHeldItemSlot});
					}

					//Update the Player's healthbar and inventory
					ReflectionUtil.invokeMethod(player, "updateScaledHealth");
					ReflectionUtil.invokeMethod(player, "updateInventory");

					if(ReflectionUtil.getMajorVersion() >= 18) {
						Class<?> packetPlayOutUpdateHealthClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutUpdateHealth");
						Object packetPlayOutUpdateHealth = ReflectionUtil.invokeConstructor(
								packetPlayOutUpdateHealthClass,
								new Class<?>[] { float.class, int.class, float.class },
								new Object[] { (float) player.getHealth() ,player.getFoodLevel(), player.getSaturation() }
						);

						ReflectionUtil.invokeMethod(playerConnection, "a", new Class<?>[] { packetClass}, new Object[] { packetPlayOutUpdateHealth });
					} else {
						ReflectionUtil.invokeMethod(entityPlayer, "triggerHealthUpdate");
					}

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
