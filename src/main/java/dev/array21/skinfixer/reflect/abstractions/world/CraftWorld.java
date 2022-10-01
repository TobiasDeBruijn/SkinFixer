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
}
