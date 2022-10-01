package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

public record PlayerInteractManager(Object inner) {
    static PlayerInteractManager getInstance(CraftPlayer craftPlayer) throws ReflectException {
        Object o;

        try {
            if (ReflectionUtil.isUseNewSpigotPackaging()) {
                o = ReflectionUtil.getObject(craftPlayer.inner(), "d");
            } else {
                o = ReflectionUtil.getObject(craftPlayer.inner(), "playerInteractManager");
            }
        } catch(Exception e) {
            throw new ReflectException(e);
        }

        return new PlayerInteractManager(o);
    }

    public Gamemode getGamemode() throws ReflectException {
        return Gamemode.getInstance(this);
    }

    public EnumGamemode getEnumGamemode() throws ReflectException {
        return EnumGamemode.getInstance(this);
    }
}
