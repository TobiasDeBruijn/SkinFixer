package dev.array21.skinfixer.reflect.abstractions.world;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.PlayerManager;
import dev.array21.skinfixer.reflect.ReflectException;

import java.lang.reflect.Field;

public record DimensionManager(Object inner) {
    static DimensionManager getInstance(CraftWorld craftWorld) throws ReflectException {
        try {
            Object inner = switch(ReflectionUtil.getMajorVersion()) {
                case 18 -> {
                    Object dimensionManagerRaw = ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "q_");
                    Class<?> holderClass = ReflectionUtil.getMinecraftClass("core.Holder");
                    yield ReflectionUtil.invokeMethod(holderClass, null, "a", new Class<?>[] { Object.class }, new Object[] { dimensionManagerRaw});
                }
                case 19 -> {
                    Field f = craftWorld.inner().getClass().getSuperclass().getDeclaredField("D");
                    f.setAccessible(true);
                    yield f.get(craftWorld.inner());
                }
                default -> throw new RuntimeException("Unsupported version");
            };

            return new DimensionManager(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
