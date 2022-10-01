package dev.array21.skinfixer.reflect.abstractions.packet;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import dev.array21.skinfixer.reflect.abstractions.player.PlayerConnection;

public interface Packet {
    Object getInner();

    default void send(PlayerConnection playerConnection) throws ReflectException {
        try {
            switch (ReflectionUtil.getMajorVersion()) {
                case 16, 17 -> ReflectionUtil.invokeMethod(
                        playerConnection.inner(),
                        "sendPacket",
                        new Class<?>[]{ getPacketClass() },
                        new Object[]{ this.getInner() });
                case 18, 19 -> ReflectionUtil.invokeMethod(
                        playerConnection.inner(),
                        "a",
                        new Class<?>[]{ getPacketClass() },
                        new Object[]{this.getInner()});
                default -> throw new RuntimeException("Unsupported version");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
    private static Class<?> getPacketClass() throws ReflectException {
        try {
            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                return ReflectionUtil.getMinecraftClass("network.protocol.Packet");
            } else {
                return ReflectionUtil.getNmsClass("Packet");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
