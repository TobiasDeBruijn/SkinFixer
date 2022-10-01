package dev.array21.skinfixer.reflect;

import com.google.common.hash.Hashing;
import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.annotations.Nullable;
import dev.array21.skinfixer.reflect.abstractions.gamemode.Gamemode;
import dev.array21.skinfixer.reflect.abstractions.packet.*;
import dev.array21.skinfixer.reflect.abstractions.player.CraftPlayer;
import dev.array21.skinfixer.reflect.abstractions.player.PlayerConnection;
import dev.array21.skinfixer.reflect.abstractions.player.PlayerInteractManager;
import dev.array21.skinfixer.reflect.abstractions.world.CraftWorld;
import dev.array21.skinfixer.reflect.abstractions.world.SeedHash;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
        PlayerInteractManager playerInteractManager = PlayerInteractManager.getInstance(craftPlayer);
        Gamemode gamemode = Gamemode.getInstance(playerInteractManager);
        SeedHash seedHash = SeedHash.getInstance(currentLoc.getWorld());

        PlayerOutRespawnPacket playerOutRespawnPacket = PlayerOutRespawnPacket.getInstance(craftWorld, playerInteractManager, gamemode, seedHash);
        PlayerOutPositionPacket playerOutPositionPacket = PlayerOutPositionPacket.getInstance(player.getLocation());
        PlayerOutHeldItemSlotPacket playerOutHeldItemSlotPacket = PlayerOutHeldItemSlotPacket.getInstance(player);

        PlayerOutInfoPacket playerOutInfoRemovePacket = PlayerOutInfoPacket.getInstance(craftPlayer, PlayerOutInfoPacket.PlayerInfoAction.REMOVE_PLAYER);
        PlayerOutInfoPacket playerOutInfoAddPacket = PlayerOutInfoPacket.getInstance(craftPlayer, PlayerOutInfoPacket.PlayerInfoAction.ADD_PLAYER);

        PlayerConnection playerConnection = PlayerConnection.getInstance(craftPlayer);

        // Remove and add the player back in
        playerOutInfoRemovePacket.send(playerConnection);
        playerOutInfoAddPacket.send(playerConnection);

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

        craftPlayer.updatePlayerScaledHealth();
        craftPlayer.updatePlayerInventory();

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
}
