package nl.bamischrijft.keycards.objects;

import nl.bamischrijft.keycards.util.SQLiteUtil;
import nl.bamischrijft.keycards.util.Util;
import org.bukkit.Location;

import java.util.Set;

public class KeyCardReader {
    private final Location location;
    private final Location placedAgainst;
    private final Set<String> cardUuids;
    private long lastUsed = 0;

    public KeyCardReader(Location location, Location placedAgainst, Set<String> cardUuids) {
        this.location = location;
        this.placedAgainst = placedAgainst;
        this.cardUuids = cardUuids;
    }

    public Location getLocation() {
        return location;
    }

    public Location getPlacedAgainst() {
        return placedAgainst;
    }

    public Set<String> getCardUuids() {
        return cardUuids;
    }

    public void addCard(String cardUuid) {
        cardUuids.add(cardUuid);
        dumpToDatabase();
    }

    public void removeCard(String cardUuid) {
        cardUuids.remove(cardUuid);
        dumpToDatabase();
    }

    public void dumpToDatabase() {
        SQLiteUtil.update("UPDATE keycardreaders SET cards=? WHERE location=?",
                String.join(",", cardUuids), Util.locationToString(location));
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }
}
