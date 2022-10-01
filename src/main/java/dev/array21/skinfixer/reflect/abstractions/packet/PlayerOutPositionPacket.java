package dev.array21.skinfixer.reflect.abstractions.packet;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public record PlayerOutPositionPacket(Object inner) implements Packet {

    @Override
    public Object getInner() {
        return this.inner;
    }

    public static PlayerOutPositionPacket getInstance(Location location) throws ReflectException {
        try {
            Class<?> clazz = getPacketPlayOutPositionClass();

            Object inner;
            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                inner = ReflectionUtil.invokeConstructor(clazz,
                        new Class<?>[] { double.class, double.class, double.class, float.class, float.class, Set.class, int.class, boolean.class },
                        new Object[] { location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), new HashSet<Enum<?>>(), 0, false });
            } else {
                inner = ReflectionUtil.invokeConstructor(clazz,
                        new Class<?>[] { double.class, double.class, double.class, float.class, float.class, Set.class, int.class },
                        new Object[] { location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), new HashSet<Enum<?>>(), 0 });
            }

            return new PlayerOutPositionPacket(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?> getPacketPlayOutPositionClass() throws ReflectException {
        try {
            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                return ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPosition");
            } else {
                return ReflectionUtil.getNmsClass("PacketPlayOutPosition");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

}
