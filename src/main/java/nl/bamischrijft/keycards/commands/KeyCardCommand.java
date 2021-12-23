package nl.bamischrijft.keycards.commands;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import nl.bamischrijft.keycards.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class KeyCardCommand implements TabCompleter, CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit command is alleen voor spelers");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("keycards.use")) {
            player.sendMessage(ChatColor.RED + "Je hebt geen permissies hiervoor");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Gebruik " + command.getUsage().replace("<command>", label));
            return true;
        }

        Player target = Util.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Speler " + args[0] + " niet gevonden!");
            return true;
        }

        int uses = 0;
        if (args.length > 1) {
            try {
                uses = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Ongeldige aantal uses " + args[1]);
                return true;
            }
        }

        UUID cardUuid = UUID.randomUUID();

        ItemStack item = new ItemStack(Material.LEATHER);
        item = NBTEditor.set(item, "keycard", "mtcustom");
        item = NBTEditor.set(item,  cardUuid.toString(), "card-uuid");

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + "KeyCard");
        meta.setLore(Arrays.asList(
                " ",
                ChatColor.DARK_AQUA + (uses == 0 ? "Unlimited uses" : "Uses: " + ChatColor.BLUE + uses)
        ));
        item.setItemMeta(meta);

        target.getInventory().addItem(item);

        if (target != player) {
            target.sendMessage(ChatColor.DARK_AQUA + "Je hebt een keycard gekregen van " + ChatColor.BLUE + player.getName());
            player.sendMessage(ChatColor.DARK_AQUA + "Je hebt een keycard gegeven aan " + ChatColor.BLUE + target.getName());
        } else player.sendMessage(ChatColor.DARK_AQUA + "Je hebt een keycard gekregen");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("keycards.use")) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[0].toLowerCase(), players, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
