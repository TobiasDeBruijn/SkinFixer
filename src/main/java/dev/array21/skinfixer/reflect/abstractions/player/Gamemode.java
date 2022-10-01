package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

import java.lang.reflect.Method;

public record Gamemode(Object inner) {
    static Gamemode getInstance(PlayerInteractManager playerInteractManager) throws ReflectException {
        Object gamemodeEnumConst;
        try {
            EnumGamemode enumGamemode = EnumGamemode.getInstance(playerInteractManager);
            int gamemodeId;

            // Get the numeric ID of the player's gamemode
            if(ReflectionUtil.getMajorVersion() >= 18) {
                gamemodeId = (int) ReflectionUtil.invokeMethod(enumGamemode.inner(), "a");
            } else {
                gamemodeId = (int) ReflectionUtil.invokeMethod(enumGamemode.inner(), "getId");
            }

            // Fetch the enum constant associated with the gamemode
            Method getGamemodeByIdMethod;
            if(ReflectionUtil.getMajorVersion() >= 18) {
                getGamemodeByIdMethod = ReflectionUtil.getMethod(enumGamemode.inner().getClass(), "a", int.class);
            } else {
                getGamemodeByIdMethod = ReflectionUtil.getMethod(enumGamemode.inner().getClass(), "getById", int.class);
            }

            gamemodeEnumConst = getGamemodeByIdMethod.invoke(null, gamemodeId);
        } catch (Exception e) {
            throw new ReflectException(e);
        }

        return new Gamemode(gamemodeEnumConst);
    }
}
