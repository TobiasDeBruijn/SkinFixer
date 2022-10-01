package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

import java.sql.Ref;

public record GameProfile(Object inner) {
    public static GameProfile getInstance(CraftPlayer craftPlayer) throws ReflectException {
        try {
            Class<?> entityHumanClass = getEntityHumanClass();

            Object inner = switch (ReflectionUtil.getMajorVersion()) {
                case 16, 17 -> ReflectionUtil.invokeMethod(entityHumanClass, craftPlayer.inner(), "getProfile");
                case 18 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 2 -> ReflectionUtil.invokeMethod(entityHumanClass, craftPlayer.inner(), "fq");
                    default -> ReflectionUtil.invokeMethod(entityHumanClass, craftPlayer.inner(), "fp");
                };
                case 19 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 1, 2 -> ReflectionUtil.invokeMethod(entityHumanClass, craftPlayer.inner(), "fy");
                    default -> ReflectionUtil.invokeMethod(entityHumanClass, craftPlayer.inner(), "fz");
                };
                default -> throw new RuntimeException("Unsupported version");
            };

            return new GameProfile(inner);

        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public PropertyMap getProperties() throws ReflectException {
        return PropertyMap.getInstance(this);
    }

    private static Class<?> getEntityHumanClass() throws ReflectException {
        try {
            return switch (ReflectionUtil.getMajorVersion()) {
                case 16 -> ReflectionUtil.getNmsClass("EntityHuman");
                case 17, 18, 19 -> ReflectionUtil.getMinecraftClass("world.entity.player.EntityHuman");
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
