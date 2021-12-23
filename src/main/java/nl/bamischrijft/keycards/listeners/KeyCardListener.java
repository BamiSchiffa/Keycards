package nl.bamischrijft.keycards.listeners;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import nl.bamischrijft.keycards.Main;
import nl.bamischrijft.keycards.objects.KeyCardReader;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;

import java.util.Arrays;

public class KeyCardListener implements Listener {
//    Reader cooldown in seconds
    private final int READER_COOLDOWN = 10;
//    The time the door is open in seconds
    private final int DOOR_OPEN_TIME = 4;

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        KeyCardReader reader = Main.getReaderManager().getReaderByLocation(block.getLocation());
        if (reader == null) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.LEATHER) return;

        String uuid = NBTEditor.getString(item,"card-uuid");
        if (uuid == null) return;

        if (!reader.getCardUuids().contains(uuid)) {
            player.sendMessage(ChatColor.RED + "Jouw kaart kan niet herkend worden door de kaartlezer");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.6F, 0.2F);
            return;
        }

        long cooldown = (long) Math.ceil(((reader.getLastUsed() + READER_COOLDOWN * 1000) - System.currentTimeMillis()) / 1000.0);
        if (cooldown > 0 && READER_COOLDOWN - cooldown > DOOR_OPEN_TIME) {
            player.sendMessage(String.format("%sHet duurt nog %d seconde%s voordat je deze kaartlezer weer kan " +
                    "gebruiken", ChatColor.RED, cooldown, (cooldown == 1) ? "" : "n"));
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.6F, 0.2F);
            return;
        } else if (cooldown > 1) {
            player.sendMessage(ChatColor.DARK_AQUA + "De deuren zijn al open");
            return;
        }


        String uses = item.getItemMeta().getLore().get(1);
        if (!uses.contains("Unlimited")) {
            int amount = Integer.parseInt(uses.substring(10));
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Deze pas kan je niet meer gebruiken");
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.6F, 0.2F);
                return;
            }

            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList(
                    " ",
                    ChatColor.DARK_AQUA + "Uses: " + ChatColor.BLUE + --amount
            ));
            item.setItemMeta(meta);
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1.2F);

        reader.setLastUsed(System.currentTimeMillis());
        Location origin = reader.getPlacedAgainst();
        for (Direction direction : Direction.values()) {
            for (int i = 1; i <= 2; i++) {
                Location doorLocation = origin.clone().add(direction.getDiffX() * i, 0, direction.getDiffZ() * i);
                if (doorLocation.getBlock().getType() != Material.IRON_DOOR_BLOCK) continue;

                BlockState state = doorLocation.getBlock().getState();
                Door door = (Door) state.getData();
                if (door.isTopHalf()) {
                    Block bottomDoorBlock = doorLocation.add(0, -1, 0).getBlock();
                    state = bottomDoorBlock.getState();
                    door = (Door) state.getData();
                }

                door.setOpen(true);
                state.update(true);

                doorLocation.getWorld().playSound(doorLocation, Sound.BLOCK_IRON_DOOR_OPEN, 1, 1);

                BlockState finalState = state;
                Door finalDoor = door;
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    finalDoor.setOpen(false);
                    finalState.update(true);
                    doorLocation.getWorld().playSound(doorLocation, Sound.BLOCK_IRON_DOOR_CLOSE, 1, 1);
                }, DOOR_OPEN_TIME * 20);
            }
        }
    }
}

enum Direction {
    NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);

    private final int diffX, diffZ;
    Direction(int diffX, int diffZ) {
        this.diffX = diffX;
        this.diffZ = diffZ;
    }

    public int getDiffX() {
        return diffX;
    }

    public int getDiffZ() {
        return diffZ;
    }
}