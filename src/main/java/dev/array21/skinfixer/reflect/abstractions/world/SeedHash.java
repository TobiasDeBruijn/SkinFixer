package dev.array21.skinfixer.reflect.abstractions.world;

import com.google.common.hash.Hashing;
import org.bukkit.World;

import java.nio.charset.StandardCharsets;

public record SeedHash(long inner) {
    public static SeedHash getInstance(World world) {
        long seed = world.getSeed();
        long seedHashed = Hashing.sha256().hashString(String.valueOf(seed), StandardCharsets.UTF_8).asLong();
        return new SeedHash(seedHashed);
    }
}
