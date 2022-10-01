package dev.array21.skinfixer.reflect.abstractions.packet;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import org.bukkit.entity.Player;

public record PlayerOutHeldItemSlotPacket(Object inner) implements Packet {

    @Override
    public Object getInner() {
        return this.inner;
    }

    public static PlayerOutHeldItemSlotPacket getInstance(Player player) throws ReflectException {
        try {
            Class<?> clazz = getPacketPlayOutHeldItemSlotClass();
            Object inner = ReflectionUtil.invokeConstructor(clazz,
                    new Class<?>[] { int.class },
                    new Object[] { player.getInventory().getHeldItemSlot() });

            return new PlayerOutHeldItemSlotPacket(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?> getPacketPlayOutHeldItemSlotClass() throws ReflectException {
        try {
            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                return ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutHeldItemSlot");
            } else {
                return ReflectionUtil.getNmsClass("PacketPlayOutHeldItemSlot");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
