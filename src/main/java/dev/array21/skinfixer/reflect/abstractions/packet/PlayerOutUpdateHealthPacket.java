package dev.array21.skinfixer.reflect.abstractions.packet;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import org.bukkit.entity.Player;

public record PlayerOutUpdateHealthPacket(Object inner) implements Packet {

    @Override
    public Object getInner() {
        return this.inner;
    }

    public static PlayerOutUpdateHealthPacket getInstance(Player player) throws ReflectException {
        try {
            Object inner = ReflectionUtil.invokeConstructor(
                    getPacketPlayOutUpdateHealthClass(),
                    new Class<?>[] { float.class, int.class, float.class },
                    new Object[] { (float) player.getHealth(), player.getFoodLevel(), player.getSaturation() }
            );

            return new PlayerOutUpdateHealthPacket(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?> getPacketPlayOutUpdateHealthClass() throws ReflectException {
        try {
            return ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutUpdateHealth");
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
