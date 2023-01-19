package dev.array21.skinfixer.reflect;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.bukkitreflectionlib.abstractions.entity.player.*;
import dev.array21.bukkitreflectionlib.abstractions.packet.*;
import dev.array21.bukkitreflectionlib.abstractions.world.CraftWorld;
import dev.array21.bukkitreflectionlib.abstractions.world.SeedHash;
import dev.array21.bukkitreflectionlib.exceptions.ReflectException;
import dev.array21.skinfixer.SkinFixer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerManager {

    public static void reloadPlayer(SkinFixer plugin, Player player) throws ReflectException {
        Location currentLoc = player.getLocation();

        // Reload for all online players, except self
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.hidePlayer(plugin, player);
            p.showPlayer(plugin, player);
        });

        CraftPlayer craftPlayer = CraftPlayer.getInstance(player);
        CraftWorld craftWorld = CraftWorld.getInstance(currentLoc.getWorld());
        PlayerInteractManager playerInteractManager = craftPlayer.getPlayerInteractManager();
        Gamemode gamemode = playerInteractManager.getGamemode();
        SeedHash seedHash = SeedHash.getInstance(currentLoc.getWorld());

        PlayerOutRespawnPacket playerOutRespawnPacket = PlayerOutRespawnPacket.getInstance(craftWorld, playerInteractManager, gamemode, seedHash);
        PlayerOutPositionPacket playerOutPositionPacket = PlayerOutPositionPacket.getInstance(player.getLocation());
        PlayerOutHeldItemSlotPacket playerOutHeldItemSlotPacket = PlayerOutHeldItemSlotPacket.getInstance(player);

        PlayerConnection playerConnection = craftPlayer.getPlayerConnection();

        // PlayerOutInfoPacket no longer exists as of 1.19.3
        if(ReflectionUtil.getMajorVersion() > 19 || (ReflectionUtil.getMajorVersion() == 19 && ReflectionUtil.getMinorVersion() == 3)) {
            ClientboundPlayerInfoRemovePacket removePacket = ClientboundPlayerInfoRemovePacket.getInstance(craftPlayer);
            ClientboundPlayerInfoUpdatePacket addPacket = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(craftPlayer);

            removePacket.send(playerConnection);
            addPacket.send(playerConnection);
        } else {
            PlayerOutInfoPacket playerOutInfoRemovePacket = PlayerOutInfoPacket.getInstance(craftPlayer, PlayerOutInfoPacket.PlayerInfoAction.REMOVE_PLAYER);
            PlayerOutInfoPacket playerOutInfoAddPacket = PlayerOutInfoPacket.getInstance(craftPlayer, PlayerOutInfoPacket.PlayerInfoAction.ADD_PLAYER);

            // Remove and add the player back in
            playerOutInfoRemovePacket.send(playerConnection);
            playerOutInfoAddPacket.send(playerConnection);
        }

        // Respawn the player
        playerOutRespawnPacket.send(playerConnection);

        // On 1.18+, we need to send the XP,
        // On older versions we need to update the Player's abilities
        if(ReflectionUtil.getMajorVersion() >= 18) {
            PlayerOutExperiencePacket playerOutExperiencePacket = PlayerOutExperiencePacket.getInstance(player);
            playerOutExperiencePacket.send(playerConnection);
        } else {
            craftPlayer.updatePlayerAbilities();
        }

        playerOutPositionPacket.send(playerConnection);
        playerOutHeldItemSlotPacket.send(playerConnection);

        PlayerUtil.updatePlayerScaledHealth(player);
        PlayerUtil.updatePlayerInventory(player);

        if(ReflectionUtil.getMajorVersion() >= 18) {
            PlayerOutUpdateHealthPacket playerOutUpdateHealthPacket = PlayerOutUpdateHealthPacket.getInstance(player);
            playerOutUpdateHealthPacket.send(playerConnection);
        } else {
            craftPlayer.triggerPlayerHealthUpdate();
        }

        // If the Player is OP, we have to toggle it off and back on really quickly for it to work
        if(player.isOp()) {
            // We run it on the Bukkit scheduler so
            // the function calling this function
            // is allowed to run off of the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.setOp(false);
                player.setOp(true);
            });
        }
    }

    public static void setSkin(Player player, String skinValue, String skinSignature) throws ReflectException {
        CraftPlayer craftPlayer = CraftPlayer.getInstance(player);

        GameProfile gameProfile = craftPlayer.getGameProfile();
        PropertyMap propertyMap = gameProfile.getProperties();

        if(propertyMap.containsTexturesKey()) {
            propertyMap.removeTexturesKey();
        }

        propertyMap.putSkinProperty(skinValue, skinSignature);
    }
}
