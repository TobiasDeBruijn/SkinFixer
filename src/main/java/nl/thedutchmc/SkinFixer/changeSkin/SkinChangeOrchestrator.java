package nl.thedutchmc.SkinFixer.changeSkin;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.hash.Hashing;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.SkinObject;
import nl.thedutchmc.SkinFixer.changeSkin.changeGameProfile.*;
import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;
import nl.thedutchmc.SkinFixer.util.ReflectionUtil;

public class SkinChangeOrchestrator {
	
	public static void changeSkinJson(String skinUrl, UUID internalUuid, UUID externalUuid, boolean slim, boolean isPremium) {

		//Everything needs to be async, because the watchdog will kill the server because it takes too long
		new BukkitRunnable() {

			@Override
			public void run() {
				
				Player player = Bukkit.getPlayer(internalUuid);
				
				String value, signature;
				
				player.sendMessage(ChatColor.GOLD + "Fetching skin value and signature...");
				
				//Fetch the skin from Mineskin.org's API
				String skinJson = null;
				if(isPremium ) {
					skinJson = GetSkin.getSkinOfValidPlayer(externalUuid.toString());
				} else {
					skinJson = GetSkin.getSkin(skinUrl, slim);
				}
								
				//Get the skin texture value, and the skin texture signature
				JSONTokener tokener = new JSONTokener(skinJson);
								
				//Descent to the Texture object
				JSONObject full = (JSONObject) tokener.nextValue();
				JSONObject data = (JSONObject) full.get("data");
				JSONObject texture = (JSONObject) data.get("texture");
				
				//Grab the value and signature
				value = (String) texture.get("value");
				signature = (String) texture.get("signature");
				
				changeSkin(value, signature, internalUuid, slim);
			}
		}.runTaskAsynchronously(SkinFixer.INSTANCE);
	}
	
	public static void changeSkinFromObject(SkinObject skin) {
		new BukkitRunnable() {
			@Override
			public void run() {
				changeSkin(skin.getValue(), skin.getSignature(), skin.getOwner(), skin.getSlim());
			}
		}.runTaskAsynchronously(SkinFixer.INSTANCE);
	}
	
	private static void changeSkin(String skinValue, String skinSignature, UUID caller, boolean slim ) {
		Player player = Bukkit.getPlayer(caller);
		
		//Store the skin to the storage file, so it can be reapplied when they join.
		if(StorageHandler.skins.containsKey(caller)) {
			SkinObject skin = StorageHandler.skins.get(caller);
			if(slim) skin.setSlim(true);
			skin.updateSkin(skinValue, skinSignature);
		} else {
			SkinObject skin = new SkinObject(caller, skinValue, skinSignature);
			if(slim) skin.setSlim(true);
			StorageHandler.skins.put(caller, skin);
		}
		
		player.sendMessage(ChatColor.GOLD + "Applying skin...");
		
		new BukkitRunnable() {
			@Override
			public void run() {
				//NMS is version dependant. So we need to set the correct class to use.
				//TODO switch this over to use reflection
				switch(SkinFixer.NMS_VERSION) {
				case "v1_16_R1": ChangeGameProfile_1_16_r1.changeProfile(player.getUniqueId(), skinValue, skinSignature); break;
				case "v1_16_R2": ChangeGameProfile_1_16_r2.changeProfile(player.getUniqueId(), skinValue, skinSignature); break;
				case "v1_16_R3": ChangeGameProfile_1_16_r3.changeProfile(player.getUniqueId(), skinValue, skinSignature); break;
				default:
					//We dont support the version that the user is running, so we inform them of this.
					//Calls to the Bukkit API may only be sync, so it's inside a BukkitRunnable
					Player p = Bukkit.getPlayer(caller);
					p.sendMessage(ChatColor.RED + "This server is using a Minecraft version that is not supported by SkinFixer!");
					p.sendMessage(ChatColor.RED + "You are running NMS version " + SkinFixer.NMS_VERSION);
				}
			}
		}.runTask(SkinFixer.INSTANCE);
		
		reloadPlayer(player);
		
		//Inform the player that we're done
		player.sendMessage(ChatColor.GOLD + "Done.");
	}
	
	private static void applySkin(Player player, String skinValue, String skinSignature) {
	}
	
	/**
	 * Reload the Player so the Player's new skin shows for other players and the Player itself
	 * @param player The Player to reload
	 */
	private static void reloadPlayer(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Location playerLocation = player.getLocation().clone();
				
			    //Reload the player for all online players
				//This is so all other Players can see the new skin
			    Bukkit.getOnlinePlayers().forEach(p -> {
			    	p.hidePlayer(SkinFixer.INSTANCE, player);
			    	p.showPlayer(SkinFixer.INSTANCE, player);
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
				    Object playerIntManager = ReflectionUtil.getObject(entityPlayer, "playerInteractManager");
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
				    Class<?> playPacketOutRespawnClass = ReflectionUtil.getNmsClass("PacketPlayOutRespawn");

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
				    Class<?> packetPlayOutPositionClass = ReflectionUtil.getNmsClass("PacketPlayOutPosition");
				    Object packetPlayOutPosition = ReflectionUtil.invokeConstructor(packetPlayOutPositionClass, 
				    		new Class<?>[] { double.class, double.class, double.class, float.class, float.class, Set.class, int.class },
				    		new Object[] { playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(), playerLocation.getYaw(), playerLocation.getPitch(), new HashSet<Enum<?>>(), 0 });
				    
				    //PacketPlayOutHeldItem
				    Class<?> packetPlayOutHeldItemSlotClass = ReflectionUtil.getNmsClass("PacketPlayOutHeldItemSlot");
				    Object packetPlayOutHeldItemSlot = ReflectionUtil.invokeConstructor(packetPlayOutHeldItemSlotClass, 
				    		new Class<?>[] { int.class },
				    		new Object[] { player.getInventory().getHeldItemSlot() });
				    
				    //Get the EntityPlayers' connection
				    Object playerConnection = ReflectionUtil.getObject(entityPlayer, "playerConnection");
				    
				    //Get the Enum constants for REMOVE_PLAYER and ADD_PLAYER
				    Class<?> packetPlayOutPlayerInfo = ReflectionUtil.getNmsClass("PacketPlayOutPlayerInfo");
				    Object removePlayerEnumConst = ReflectionUtil.getEnum(packetPlayOutPlayerInfo, "EnumPlayerInfoAction", "REMOVE_PLAYER");
				    Object addPlayerEnumConst = ReflectionUtil.getEnum(packetPlayOutPlayerInfo, "EnumPlayerInfoAction", "ADD_PLAYER");
				    				    
				    //Create an Array of EntityPlayer with size = 1 and add our player to it
				    Object entityPlayerArr = Array.newInstance(entityPlayer.getClass(), 1);
				    Array.set(entityPlayerArr, 0, entityPlayer);
				    
				    //Construct a PacketPlayOutPlayerInfo with intention REMOVE_PLAYER
				    Object packetPlayOutRemovePlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfo, 
				    		new Class<?>[] { removePlayerEnumConst.getClass(), entityPlayerArr.getClass() }, 
				    		new Object[] { removePlayerEnumConst, entityPlayerArr });
				    
				    //Construct a PacketPlayOutPlayerInfo with intention ADD_PLAYER
				    Object packetPlayOutAddPlayer = ReflectionUtil.invokeConstructor(packetPlayOutPlayerInfo, 
				    		new Class<?>[] { addPlayerEnumConst.getClass(), entityPlayerArr.getClass() },
				    		new Object[] { addPlayerEnumConst, entityPlayerArr });
				    
				    //Get the generic Packet class
				    Class<?> packetClass = ReflectionUtil.getNmsClass("Packet");
				    
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
		            	Bukkit.getScheduler().runTask(SkinFixer.INSTANCE, () -> {
		            		player.setOp(false);
		            		player.setOp(true);
		            	});
		            }
			    } catch(Exception e) {
			    	e.printStackTrace();
			    }
			}
		}.runTask(SkinFixer.INSTANCE);
	}
}
