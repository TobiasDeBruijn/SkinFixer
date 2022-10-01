package dev.array21.skinfixer.reflect.abstractions.world;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

import java.lang.reflect.Field;

public record DimensionKey(Object inner) {
    static DimensionKey getInstance(CraftWorld craftWorld) throws ReflectException {
        try {
            Object inner = switch(ReflectionUtil.getMajorVersion()) {
                case 16, 17 -> ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "getDimensionKey");
                case 18 -> ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "aa");
                case 19 -> {
                    Field f = craftWorld.inner().getClass().getSuperclass().getDeclaredField("I");
                    f.setAccessible(true);
                    yield f.get(craftWorld.inner());
                }
                default -> throw new RuntimeException("Unsupported version");
            };

            return new DimensionKey(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
