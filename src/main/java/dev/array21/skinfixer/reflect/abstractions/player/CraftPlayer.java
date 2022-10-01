package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import org.bukkit.entity.Player;

public record CraftPlayer(Object inner) {
    public static CraftPlayer getInstance(Player player) throws ReflectException {
        try {
            final Class<?> clazz = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
            final Object entityPlayer = ReflectionUtil.invokeMethod(clazz, player, "getHandle");

            return new CraftPlayer(entityPlayer);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public void updatePlayerAbilities() throws ReflectException {
        try {
            ReflectionUtil.invokeMethod(this.inner, "updateAbilities");
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public void updatePlayerScaledHealth() throws  ReflectException {
        try {
            ReflectionUtil.invokeMethod(this.inner, "updateScaledHealth");
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public void updatePlayerInventory() throws ReflectException {
        try {
            ReflectionUtil.invokeMethod(this.inner, "updateInventory");
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public void triggerPlayerHealthUpdate() throws ReflectException {
        try {
            ReflectionUtil.invokeMethod(this.inner, "triggerHealthUpdate");
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
