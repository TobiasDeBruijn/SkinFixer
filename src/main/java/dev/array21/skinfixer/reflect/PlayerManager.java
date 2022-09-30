package dev.array21.skinfixer.reflect;

import com.google.common.hash.Hashing;
import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.annotations.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Ref;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PlayerManager {

    public static void reloadPlayer(SkinFixer plugin, Player player) throws ReflectException {
        Location currentLoc = player.getLocation();

        // Reload for all online players, except self
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.hidePlayer(plugin, player);
            p.showPlayer(plugin, player);
        });

        CraftPlayer craftPlayer = getCraftPlayer(player);
        CraftWorld craftWorld = getCraftWorld(currentLoc.getWorld());
        PlayerInteractManager playerInteractManager = getPlayerInteractManager(craftPlayer);
        Gamemode gamemode = getGamemode(playerInteractManager);
        SeedHash seedHash = getSeedHash(currentLoc.getWorld());

        PlayerOutRespawnPacket playerOutRespawnPacket = getPlayerRespawnPacket(craftWorld, playerInteractManager, gamemode, seedHash);
        PlayerOutPositionPacket playerOutPositionPacket = getPlayerOutPositionPacket(player.getLocation());
        PlayerOutHeldItemSlotPacket playerOutHeldItemSlotPacket = getPlayerOutHeldItemSlotPacket(player);

    }

    private interface Packet {}

    private record CraftPlayer(Object inner) {}

    private static CraftPlayer getCraftPlayer(Player player) throws ReflectException {
        try {
            final Class<?> clazz = ReflectionUtil.getBukkitClass("entity.CraftPlayer");
            final Object entityPlayer = ReflectionUtil.invokeMethod(clazz, player, "getHandle");

            return new CraftPlayer(entityPlayer);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private record CraftWorld(Object inner) {}

    private static CraftWorld getCraftWorld(World world) throws ReflectException {
        try {
            final Class<?> clazz = ReflectionUtil.getBukkitClass("CraftWorld");
            final Object entityPlayer = ReflectionUtil.invokeMethod(clazz, world, "getHandle");

            return new CraftWorld(entityPlayer);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private record PlayerInteractManager(Object inner) {}

    private static PlayerInteractManager getPlayerInteractManager(CraftPlayer craftPlayer) throws ReflectException {
        Object o;

        try {
            if (ReflectionUtil.isUseNewSpigotPackaging()) {
                o = ReflectionUtil.getObject(craftPlayer.inner, "d");
            } else {
                o = ReflectionUtil.getObject(craftPlayer.inner, "playerInteractManager");
            }
        } catch(Exception e) {
            throw new ReflectException(e);
        }

        return new PlayerInteractManager(o);
    }

    private record EnumGamemode(Enum<?> inner) {}

    private static EnumGamemode getEnumGamemode(PlayerInteractManager playerInteractManager) throws ReflectException {
        Enum<?> enumGamemode;

        try {
            if (ReflectionUtil.getMajorVersion() >= 18) {
                enumGamemode = (Enum<?>) ReflectionUtil.invokeMethod(playerInteractManager.inner, "b");
            } else {
                enumGamemode = (Enum<?>) ReflectionUtil.invokeMethod(playerInteractManager.inner, "getGameMode");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }

        return new EnumGamemode(enumGamemode);
    }

    private record Gamemode(Object inner) {}

    private static Gamemode getGamemode(PlayerInteractManager playerInteractManager) throws ReflectException {
        Object gamemodeEnumConst;
        try {
            EnumGamemode enumGamemode = getEnumGamemode(playerInteractManager);
            int gamemodeId;

            // Get the numeric ID of the player's gamemode
            if(ReflectionUtil.getMajorVersion() >= 18) {
                gamemodeId = (int) ReflectionUtil.invokeMethod(enumGamemode.inner, "a");
            } else {
                gamemodeId = (int) ReflectionUtil.invokeMethod(enumGamemode.inner, "getId");
            }

            // Fetch the enum constant associated with the gamemode
            Method getGamemodeByIdMethod;
            if(ReflectionUtil.getMajorVersion() >= 18) {
                getGamemodeByIdMethod = ReflectionUtil.getMethod(enumGamemode.getClass(), "a", int.class);
            } else {
                getGamemodeByIdMethod = ReflectionUtil.getMethod(enumGamemode.getClass(), "getById", int.class);
            }

            gamemodeEnumConst = getGamemodeByIdMethod.invoke(null, gamemodeId);
        } catch (Exception e) {
            throw new ReflectException(e);
        }

        return new Gamemode(gamemodeEnumConst);
    }

    private record SeedHash(long inner) {}

    private static SeedHash getSeedHash(World world) {
        long seed = world.getSeed();
        long seedHashed = Hashing.sha256().hashString(String.valueOf(seed), StandardCharsets.UTF_8).asLong();
        return new SeedHash(seedHashed);
    }

    private record PlayerOutRespawnPacket(Object inner) implements Packet {}

    private static PlayerOutRespawnPacket getPlayerRespawnPacket(CraftWorld craftWorld, PlayerInteractManager playerInteractManager, Gamemode gamemode, SeedHash seedHash) throws ReflectException {
        Class<?> packetClass = getPacketOutRespawnClass();
        DimensionKey dimensionKey = getDimensionKey(craftWorld);
        EnumGamemode enumGamemode = getEnumGamemode(playerInteractManager);
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
                            getPlayerRespawnPacketConstructorArguments(craftWorld, gamemode, dimensionKey, getDimensionManager(craftWorld), seedHash));
                };
                case 17, 18, 19 -> ReflectionUtil.invokeConstructor(
                        packetClass,
                        getPlayerRespawnPacketConstructorClasses(craftWorld, dimensionKey, enumGamemode),
                        getPlayerRespawnPacketConstructorArguments(craftWorld, gamemode, dimensionKey, getDimensionManager(craftWorld), seedHash));
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

    private record DimensionManager(Object inner) {}

    private static DimensionManager getDimensionManager(CraftWorld craftWorld) throws ReflectException {
        try {
            Object inner = switch(ReflectionUtil.getMajorVersion()) {
                case 18 -> {
                    Object dimensionManagerRaw = ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "q_");
                    Class<?> holderClass = ReflectionUtil.getMinecraftClass("core.Holder");
                    yield ReflectionUtil.invokeMethod(holderClass, null, "a", new Class<?>[] { Object.class }, new Object[] { dimensionManagerRaw});
                }
                case 19 -> {
                    Field f = craftWorld.inner.getClass().getSuperclass().getDeclaredField("D");
                    f.setAccessible(true);
                    yield f.get(craftWorld.inner);
                }
                default -> throw new RuntimeException("Unsupported version");
            };

            return new DimensionManager(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private record DimensionKey(Object inner) {}

    private static DimensionKey getDimensionKey(CraftWorld craftWorld) throws ReflectException {
        try {
            Object inner = switch(ReflectionUtil.getMajorVersion()) {
                case 16, 17 -> ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "getDimensionKey");
                case 18 -> ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "aa");
                case 19 -> {
                    Field f = craftWorld.inner.getClass().getSuperclass().getDeclaredField("I");
                    f.setAccessible(true);
                    yield f.get(craftWorld.inner);
                }
                default -> throw new RuntimeException("Unsupported version");
            };

            return new DimensionKey(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static boolean isDebugWorld(CraftWorld craftWorld) throws ReflectException {
        try {
            return (boolean) switch(ReflectionUtil.getMajorVersion()) {
                case 19 -> ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "ae");
                case 18 -> ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "ad");
                case 16, 17 -> ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "isDebugWorld");
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static boolean isFlatWorld(CraftWorld craftWorld) throws ReflectException {
        try {
            return (boolean) switch(ReflectionUtil.getMajorVersion()) {
                case 19 -> ReflectionUtil.invokeMethod(craftWorld.inner, "A");
                case 18 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 2 -> ReflectionUtil.invokeMethod(craftWorld.inner, "C");
                    default -> ReflectionUtil.invokeMethod(craftWorld.inner, "D");
                };
                case 16, 17 -> ReflectionUtil.invokeMethod(craftWorld.inner, "isFlatWorld");
                default -> throw new RuntimeException("Unsupported version");
            };
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?>[] getPlayerRespawnPacketConstructorClasses(CraftWorld craftWorld, DimensionKey dimensionKey, EnumGamemode enumGamemode) throws ReflectException {
        try {
            return switch(ReflectionUtil.getMajorVersion()) {
                case 16 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 0, 1 -> {
                        Object typeKey = ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "getTypeKey");
                        yield new Class<?>[] {
                                typeKey.getClass(),
                                dimensionKey.inner.getClass(),
                                long.class,
                                enumGamemode.inner.getClass(),
                                enumGamemode.inner.getClass(),
                                boolean.class,
                                boolean.class,
                                boolean.class
                        };
                    }
                    default -> {
                        DimensionManager dimensionManager = getDimensionManager(craftWorld);
                        yield new Class<?>[] {
                                dimensionManager.inner.getClass(),
                                dimensionKey.inner.getClass(),
                                long.class,
                                enumGamemode.inner.getClass(),
                                enumGamemode.inner.getClass(),
                                boolean.class,
                                boolean.class,
                                boolean.class,
                        };
                    }
                };
                case 17, 18 -> {
                    DimensionManager dimensionManager = getDimensionManager(craftWorld);
                    yield new Class<?>[] {
                            dimensionManager.inner.getClass(),
                            dimensionKey.inner.getClass(),
                            long.class,
                            enumGamemode.inner.getClass(),
                            enumGamemode.inner.getClass(),
                            boolean.class,
                            boolean.class,
                            boolean.class,
                    };
                }
                case 19 -> {
                    DimensionManager dimensionManager = getDimensionManager(craftWorld);
                    yield new Class<?>[] {
                            dimensionManager.inner.getClass(),
                            dimensionKey.inner.getClass(),
                            long.class,
                            enumGamemode.inner.getClass(),
                            enumGamemode.inner.getClass(),
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
            boolean isDebugWorld = isDebugWorld(craftWorld);
            boolean isFlatWorld = isFlatWorld(craftWorld);

            return switch (ReflectionUtil.getMajorVersion()) {
                case 16 -> switch(ReflectionUtil.getMinorVersion()) {
                    case 0, 1 -> {
                        Object typeKey = ReflectionUtil.invokeMethod(craftWorld.inner.getClass().getSuperclass(), craftWorld.inner, "getTypeKey");
                        yield new Object[] {
                                typeKey,
                                dimensionKey.inner,
                                seedHash.inner,
                                gamemode.inner,
                                gamemode.inner,
                                isDebugWorld,
                                isFlatWorld,
                                true,
                        };
                    }
                    default -> new Object[] {
                            dimensionManager.inner,
                            dimensionKey.inner,
                            seedHash.inner,
                            gamemode.inner,
                            gamemode.inner,
                            isDebugWorld,
                            isFlatWorld,
                            true,
                    };
                };
                case 17, 18 -> new Object[] {
                        dimensionManager.inner,
                        dimensionKey.inner,
                        seedHash.inner,
                        gamemode.inner,
                        gamemode.inner,
                        isDebugWorld,
                        isFlatWorld,
                        true,
                };
                case 19 -> new Object[] {
                        dimensionManager.inner,
                        dimensionKey.inner,
                        seedHash.inner,
                        gamemode.inner,
                        gamemode.inner,
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


    private record PlayerOutPositionPacket(Object inner) implements Packet {}

    private static PlayerOutPositionPacket getPlayerOutPositionPacket(Location location) throws ReflectException {
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

    private record PlayerOutHeldItemSlotPacket(Object inner) implements Packet {}

    private static PlayerOutHeldItemSlotPacket getPlayerOutHeldItemSlotPacket(Player player) throws ReflectException {
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

    private enum PlayerInfoAction {
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

    private record PlayerOutInfoPacket(Object inner) implements Packet {}

    private static PlayerOutInfoPacket getPlayerOutInfoPacket(CraftPlayer craftPlayer, PlayerInfoAction playerInfoAction) throws ReflectException {
        try {
            Object entityPlayerArr = Array.newInstance(craftPlayer.inner.getClass(), 1);
            Array.set(entityPlayerArr, 0, craftPlayer.inner);

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

    private static Class<?> getPacketPlayOutExperienceClass() throws ReflectException {
        try {
            if(ReflectionUtil.getMajorVersion() >= 18) {
                return ReflectionUtil.getMinecraftClass("network.protocol.game.PacketPlayOutExperience");
            } else {
                throw new IllegalStateException("This Minecraft version does not require the PacketPlayOutExperience packet to be send");
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private record PlayerOutExperiencePacket(Object inner) implements Packet {}

    private static PlayerOutExperiencePacket getPlayerOutExperiencePacket(Player player) throws ReflectException {
        try {
            Object inner = ReflectionUtil.invokeConstructor(getPacketPlayOutExperienceClass(),
                    new Class<?>[] { float.class, int.class, int.class },
                    new Object[] { player.getExp(), player.getTotalExperience(), player.getLevel() });

            return new PlayerOutExperiencePacket(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
