package nl.bamischrijft.keycards.managers;

import nl.bamischrijft.keycards.Main;
import nl.bamischrijft.keycards.objects.KeyCardReader;
import nl.bamischrijft.keycards.util.SQLiteUtil;
import nl.bamischrijft.keycards.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class KeyCardReaderManager {
    private final HashMap<Location, KeyCardReader> readers = new HashMap<>();

    public KeyCardReaderManager() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            try {
                CachedRowSet results = SQLiteUtil.query("SELECT * FROM keycardreaders").get();

                while (results.next()) {
                    Location location = Util.locationFromString(results.getString("location"));
                    Location placedAgainst = Util.locationFromString(results.getString("placed_against"));
                    HashSet<String> cards = new HashSet<>(Arrays.asList(results.getString("cards").split(",")));
                    readers.put(location, new KeyCardReader(location, placedAgainst, cards));
                }
            } catch (InterruptedException | ExecutionException | SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public KeyCardReader getReaderByLocation(Location location) {
        return readers.get(location);
    }

    public void deleteReaderByLocation(Location location) {
        readers.remove(location);
        SQLiteUtil.update("DELETE FROM keycardreaders WHERE location=?", Util.locationToString(location));
    }

    public void addReader(KeyCardReader reader) {
        readers.put(reader.getLocation(), reader);
        SQLiteUtil.update("INSERT INTO keycardreaders (location, placed_against, cards) VALUES (?, ?, ?)",
                Util.locationToString(reader.getLocation()), Util.locationToString(reader.getPlacedAgainst()),
                String.join(",", reader.getCardUuids()));
    }
}
