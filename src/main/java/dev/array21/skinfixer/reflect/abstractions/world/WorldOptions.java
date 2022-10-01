package dev.array21.skinfixer.reflect.abstractions.world;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

public class WorldOptions {
    public static boolean isDebugWorld(CraftWorld craftWorld) throws ReflectException {
        try {
            return (boolean) switch(ReflectionUtil.getMajorVersion()) {
                case 19 -> ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "ae");
                case 18 -> ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "ad");
                case 16, 17 -> ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "isDebugWorld");
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public static boolean isFlatWorld(CraftWorld craftWorld) throws ReflectException {
        try {
            return (boolean) switch(ReflectionUtil.getMajorVersion()) {
                case 19 -> ReflectionUtil.invokeMethod(craftWorld.inner(), "A");
                case 18 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 2 -> ReflectionUtil.invokeMethod(craftWorld.inner(), "C");
                    default -> ReflectionUtil.invokeMethod(craftWorld.inner(), "D");
                };
                case 16, 17 -> ReflectionUtil.invokeMethod(craftWorld.inner(), "isFlatWorld");
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
