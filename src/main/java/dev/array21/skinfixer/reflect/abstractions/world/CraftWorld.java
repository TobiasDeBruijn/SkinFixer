package dev.array21.skinfixer.reflect.abstractions.world;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import org.bukkit.World;

public record CraftWorld(Object inner) {
    public static CraftWorld getInstance(World world) throws ReflectException {
        try {
            final Class<?> clazz = ReflectionUtil.getBukkitClass("CraftWorld");
            final Object entityPlayer = ReflectionUtil.invokeMethod(clazz, world, "getHandle");

            return new CraftWorld(entityPlayer);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public DimensionKey getDimensionKey() throws ReflectException {
        return DimensionKey.getInstance(this);
    }

    public DimensionManager getDimensionManager() throws ReflectException {
        return DimensionManager.getInstance(this);
    }

    public boolean isDebugWorld() throws ReflectException {
        try {
            return (boolean) switch(ReflectionUtil.getMajorVersion()) {
                case 19 -> ReflectionUtil.invokeMethod(this.inner.getClass().getSuperclass(), this.inner, "ae");
                case 18 -> ReflectionUtil.invokeMethod(this.inner.getClass().getSuperclass(), this.inner, "ad");
                case 16, 17 -> ReflectionUtil.invokeMethod(this.inner.getClass().getSuperclass(), this.inner, "isDebugWorld");
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public boolean isFlatWorld() throws ReflectException {
        try {
            return (boolean) switch(ReflectionUtil.getMajorVersion()) {
                case 19 -> ReflectionUtil.invokeMethod(this.inner, "A");
                case 18 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 2 -> ReflectionUtil.invokeMethod(this.inner, "C");
                    default -> ReflectionUtil.invokeMethod(this.inner, "D");
                };
                case 16, 17 -> ReflectionUtil.invokeMethod(this.inner, "isFlatWorld");
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
