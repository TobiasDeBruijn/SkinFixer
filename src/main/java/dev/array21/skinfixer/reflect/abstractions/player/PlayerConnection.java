package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

public record PlayerConnection(Object inner) {

    public static PlayerConnection getInstance(CraftPlayer craftPlayer) throws ReflectException {
        try {
            Object inner;

            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                inner = ReflectionUtil.getObject(craftPlayer.inner(), "b");
            } else {
                inner = ReflectionUtil.getObject(craftPlayer.inner(), "playerConnection");
            }

            return new PlayerConnection(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
