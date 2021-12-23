package nl.bamischrijft.keycards.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {
    private static final Pattern UUID_REGEX = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    public static Player getPlayer(String input) {
        if (UUID_REGEX.matcher(input).matches()) return Bukkit.getPlayer(UUID.fromString(input));
        return Bukkit.getPlayer(input);
    }

    public static Location locationFromString(String locationString) {
        int[] coordinates = Arrays.stream(locationString.split(",")).limit(3).mapToInt(Integer::parseInt).toArray();
        String worldName = Arrays.stream(locationString.split(",")).skip(3).collect(Collectors.joining(","));

        return new Location(Bukkit.getWorld(worldName), coordinates[0], coordinates[1], coordinates[2]);
    }

    public static String locationToString(Location location) {
        return String.format("%d,%d,%d,%s", location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                location.getWorld().getName());
    }
}
