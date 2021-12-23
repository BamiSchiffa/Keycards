package nl.bamischrijft.keycards.listeners;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import nl.bamischrijft.keycards.Main;
import nl.bamischrijft.keycards.objects.KeyCardReader;
import nl.bamischrijft.keycards.util.Util;
import nl.bamischrijft.keycards.managers.KeyCardReaderManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Locale;

public class KeyCardReaderListener implements Listener {
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || item.getType() != Material.SKULL_ITEM) return;
        if (!item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().contains("Keycard Reader")) return;

        Player player = event.getPlayer();
        Location placedAgainst = event.getBlockAgainst().getLocation();

        KeyCardReader reader = new KeyCardReader(event.getBlock().getLocation(), placedAgainst, new HashSet<>());

        Main.getReaderManager().addReader(reader);

        player.sendMessage(ChatColor.DARK_AQUA + "Je hebt een keycard reader geplaatst\n" +
                "Shiftclick erop met een keycard om hem toe te voegen");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();

        KeyCardReaderManager readerManager = Main.getReaderManager();
        if (readerManager.getReaderByLocation(location) != null) {
            readerManager.deleteReaderByLocation(location);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.LEATHER) return;
        String uuid = NBTEditor.getString(item, "card-uuid");
        if (uuid == null) return;

        KeyCardReader reader = Main.getReaderManager().getReaderByLocation(event.getClickedBlock().getLocation());

        boolean add = !reader.getCardUuids().contains(uuid);

        BaseComponent[] messageBase = new ComponentBuilder("Klik ").color(ChatColor.DARK_AQUA)
                .append(new ComponentBuilder("hier").color(ChatColor.BLUE).create())
                .append(new ComponentBuilder(" om deze keycard " + (add ? "toe te voegen" : "te verwijderen") +
                        " van deze kaartlezer").color(ChatColor.DARK_AQUA).create()).create();
        TextComponent message = new TextComponent(messageBase);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/cardreadermodify %s %s %s", Util.locationToString(reader.getLocation()),
                        add ? "add" : "remove", uuid)));

        player.spigot().sendMessage(message);

        event.setCancelled(true);
    }

    @EventHandler
    public void onCardReaderModify(PlayerCommandPreprocessEvent event) {
        String[] commandComponents = event.getMessage().split(" ");
        if (commandComponents.length < 4) return;
        if (!commandComponents[0].equalsIgnoreCase("/cardreadermodify")) return;

        Player player = event.getPlayer();

        Location location = Util.locationFromString(commandComponents[1]);
        String action = commandComponents[2];
        String uuid = commandComponents[3];

        KeyCardReaderManager readerManager = Main.getReaderManager();
        KeyCardReader reader = readerManager.getReaderByLocation(location);
        if (reader == null) return;

        boolean contains = reader.getCardUuids().contains(uuid);

        if (action.equalsIgnoreCase("add")) {
            if (contains) {
                player.sendMessage(ChatColor.RED + "Deze keycard is al toegevoegd aan de kaartlezer");
            } else {
                player.sendMessage(ChatColor.DARK_AQUA + "Deze keycard is toegevoegd aan de kaartlezer");
                reader.addCard(uuid);
            }
        } else if (action.equalsIgnoreCase("remove")) {
            if (contains) {
                player.sendMessage(ChatColor.DARK_AQUA + "Deze keycard is verwijderd van de kaartlezer");
                reader.removeCard(uuid);
            } else {
                player.sendMessage(ChatColor.RED + "Deze keycard is al verwijderd van de kaartlezer");
            }
        }

        event.setCancelled(true);
    }
}
