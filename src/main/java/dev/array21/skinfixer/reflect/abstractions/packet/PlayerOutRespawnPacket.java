package dev.array21.skinfixer.reflect.abstractions.packet;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.annotations.Nullable;
import dev.array21.skinfixer.reflect.ReflectException;
import dev.array21.skinfixer.reflect.abstractions.player.EnumGamemode;
import dev.array21.skinfixer.reflect.abstractions.player.Gamemode;
import dev.array21.skinfixer.reflect.abstractions.player.PlayerInteractManager;
import dev.array21.skinfixer.reflect.abstractions.world.*;

import java.util.Optional;

public record PlayerOutRespawnPacket(Object inner) implements Packet{

    @Override
    public Object getInner() {
        return this.inner;
    }

    public static PlayerOutRespawnPacket getInstance(CraftWorld craftWorld, PlayerInteractManager playerInteractManager, Gamemode gamemode, SeedHash seedHash) throws ReflectException {
        Class<?> packetClass = getPacketOutRespawnClass();
        DimensionKey dimensionKey = craftWorld.getDimensionKey();
        EnumGamemode enumGamemode = playerInteractManager.getEnumGamemode();
        try {
            Object packet = switch(ReflectionUtil.getMajorVersion()) {
                case 16 -> switch (ReflectionUtil.getMinorVersion()) {
                    case 0, 1 -> ReflectionUtil.invokeConstructor(
                            packetClass,
                            getPlayerRespawnPacketConstructorClasses(craftWorld, dimensionKey, enumGamemode),
                            getPlayerRespawnPacketConstructorArguments(craftWorld, gamemode, dimensionKey, null, seedHash));
                    default -> ReflectionUtil.invokeConstructor(
                            packetClass,
                            getPlayerRespawnPacketConstructorClasses(craftWorld, dimensionKey, enumGamemode),
                            getPlayerRespawnPacketConstructorArguments(craftWorld, gamemode, dimensionKey, craftWorld.getDimensionManager(), seedHash));
                };
                case 17, 18, 19 -> ReflectionUtil.invokeConstructor(
                        packetClass,
                        getPlayerRespawnPacketConstructorClasses(craftWorld, dimensionKey, enumGamemode),
                        getPlayerRespawnPacketConstructorArguments(craftWorld, gamemode, dimensionKey, craftWorld.getDimensionManager(), seedHash));
                default -> throw new RuntimeException("Unsupported version");
            };

            return new PlayerOutRespawnPacket(packet);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?> getPacketOutRespawnClass() throws ReflectException {
        Class<?> playPacketOutRespawnClass;
        try {
            if(ReflectionUtil.isUseNewSpigotPackaging()) {
                playPacketOutRespawnClass = ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutRespawn");
            } else {
                playPacketOutRespawnClass = ReflectionUtil.getNmsClass("PacketPlayOutRespawn");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }

        return playPacketOutRespawnClass;
    }

    private static Class<?>[] getPlayerRespawnPacketConstructorClasses(CraftWorld craftWorld, DimensionKey dimensionKey, EnumGamemode enumGamemode) throws ReflectException {
        try {
            return switch(ReflectionUtil.getMajorVersion()) {
                case 16 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 0, 1 -> {
                        Object typeKey = ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "getTypeKey");
                        yield new Class<?>[] {
                                typeKey.getClass(),
                                dimensionKey.inner().getClass(),
                                long.class,
                                enumGamemode.inner().getClass(),
                                enumGamemode.inner().getClass(),
                                boolean.class,
                                boolean.class,
                                boolean.class
                        };
                    }
                    default -> {
                        DimensionManager dimensionManager = craftWorld.getDimensionManager();
                        yield new Class<?>[] {
                                dimensionManager.inner().getClass(),
                                dimensionKey.inner().getClass(),
                                long.class,
                                enumGamemode.inner().getClass(),
                                enumGamemode.inner().getClass(),
                                boolean.class,
                                boolean.class,
                                boolean.class,
                        };
                    }
                };
                case 17, 18 -> {
                    DimensionManager dimensionManager = craftWorld.getDimensionManager();
                    yield new Class<?>[] {
                            dimensionManager.inner().getClass(),
                            dimensionKey.inner().getClass(),
                            long.class,
                            enumGamemode.inner().getClass(),
                            enumGamemode.inner().getClass(),
                            boolean.class,
                            boolean.class,
                            boolean.class,
                    };
                }
                case 19 -> {
                    DimensionManager dimensionManager = craftWorld.getDimensionManager();
                    yield new Class<?>[] {
                            dimensionManager.inner().getClass(),
                            dimensionKey.inner().getClass(),
                            long.class,
                            enumGamemode.inner().getClass(),
                            enumGamemode.inner().getClass(),
                            boolean.class,
                            boolean.class,
                            boolean.class,
                            Optional.class
                    };
                }
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Object[] getPlayerRespawnPacketConstructorArguments(CraftWorld craftWorld, Gamemode gamemode, DimensionKey dimensionKey, @Nullable DimensionManager dimensionManager, SeedHash seedHash) throws ReflectException {
        try {
            boolean isDebugWorld = craftWorld.isDebugWorld();
            boolean isFlatWorld = craftWorld.isFlatWorld();

            return switch (ReflectionUtil.getMajorVersion()) {
                case 16 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 0, 1 -> {
                        Object typeKey = ReflectionUtil.invokeMethod(craftWorld.inner().getClass().getSuperclass(), craftWorld.inner(), "getTypeKey");
                        yield new Object[] {
                                typeKey,
                                dimensionKey.inner(),
                                seedHash.inner(),
                                gamemode.inner(),
                                gamemode.inner(),
                                isDebugWorld,
                                isFlatWorld,
                                true,
                        };
                    }
                    default -> new Object[] {
                            dimensionManager.inner(),
                            dimensionKey.inner(),
                            seedHash.inner(),
                            gamemode.inner(),
                            gamemode.inner(),
                            isDebugWorld,
                            isFlatWorld,
                            true,
                    };
                };
                case 17, 18 -> new Object[] {
                        dimensionManager.inner(),
                        dimensionKey.inner(),
                        seedHash.inner(),
                        gamemode.inner(),
                        gamemode.inner(),
                        isDebugWorld,
                        isFlatWorld,
                        true,
                };
                case 19 -> new Object[] {
                        dimensionManager.inner(),
                        dimensionKey.inner(),
                        seedHash.inner(),
                        gamemode.inner(),
                        gamemode.inner(),
                        isDebugWorld,
                        isFlatWorld,
                        true,
                        Optional.empty(),
                };
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
