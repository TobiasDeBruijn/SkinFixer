package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static void updatePlayerScaledHealth(Player player) throws ReflectException {
        try {
            ReflectionUtil.invokeMethod(player, "updateScaledHealth");
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public static void updatePlayerInventory(Player player) throws ReflectException {
        try {
            ReflectionUtil.invokeMethod(player, "updateInventory");
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
