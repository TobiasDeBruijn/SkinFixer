package dev.array21.skinfixer.reflect.abstractions.packet;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;
import dev.array21.skinfixer.reflect.abstractions.player.CraftPlayer;

import java.lang.reflect.Array;

public record PlayerOutInfoPacket(Object inner) implements Packet {

    @Override
    public Object getInner() {
        return this.inner;
    }

    public static PlayerOutInfoPacket getInstance(CraftPlayer craftPlayer, PlayerInfoAction playerInfoAction) throws ReflectException {
        try {
            Object entityPlayerArr = Array.newInstance(craftPlayer.inner().getClass(), 1);
            Array.set(entityPlayerArr, 0, craftPlayer.inner());

            Class<?> clazz = getPacketPlayOutPlayerInfoClass();
            PlayerInfoActionConstant playerInfoActionConstant = getPlayerInfoActionConstant(playerInfoAction);

            Object inner;
            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                Class<?> enumPlayerInfoActionClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

                inner = ReflectionUtil.invokeConstructor(clazz,
                        new Class<?>[] { enumPlayerInfoActionClass, entityPlayerArr.getClass() },
                        new Object[] { playerInfoActionConstant.inner, entityPlayerArr });
            } else {
                inner = ReflectionUtil.invokeConstructor(clazz,
                        new Class<?>[] { playerInfoActionConstant.inner.getClass(), entityPlayerArr.getClass() },
                        new Object[] { playerInfoActionConstant.inner, entityPlayerArr });
            }

            return new PlayerOutInfoPacket(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?> getPacketPlayOutPlayerInfoClass() throws ReflectException {
        try {
            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                return ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutPlayerInfo");
            } else {
                return ReflectionUtil.getNmsClass("PacketPlayOutPlayerInfo");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private record PlayerInfoActionConstant(Object inner) {}

    public enum PlayerInfoAction {
        ADD_PLAYER,
        REMOVE_PLAYER;
    }

    private static PlayerInfoActionConstant getPlayerInfoActionConstant(PlayerInfoAction playerInfoAction) throws ReflectException {
        try {
            String constantName = switch(playerInfoAction) {
                case ADD_PLAYER -> "ADD_PLAYER";
                case REMOVE_PLAYER -> "REMOVE_PLAYER";
            };

            Object inner = ReflectionUtil.getEnum(getPacketPlayOutPlayerInfoClass(), "EnumPlayerInfoAction", constantName);
            return new PlayerInfoActionConstant(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
