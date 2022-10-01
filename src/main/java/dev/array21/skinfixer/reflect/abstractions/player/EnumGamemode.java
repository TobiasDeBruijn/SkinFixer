package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

public record EnumGamemode(Enum<?> inner) {
    static EnumGamemode getInstance(PlayerInteractManager playerInteractManager) throws ReflectException {
        Enum<?> enumGamemode;

        try {
            if (ReflectionUtil.getMajorVersion() >= 18) {
                enumGamemode = (Enum<?>) ReflectionUtil.invokeMethod(playerInteractManager.inner(), "b");
            } else {
                enumGamemode = (Enum<?>) ReflectionUtil.invokeMethod(playerInteractManager.inner(), "getGameMode");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }

        return new EnumGamemode(enumGamemode);
    }
}