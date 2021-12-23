package nl.bamischrijft.keycards.commands;

import com.mojang.authlib.properties.Property;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import com.mojang.authlib.GameProfile;
import org.bukkit.SkullType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;

public class KeyCardReaderCommand implements TabCompleter, CommandExecutor {
    private final String SKULL_URL = "http://textures.minecraft.net/texture/27818c0f546de076042598293614789b11dd850694d5657381c4f939137f58d0";

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

        if (args[0].equalsIgnoreCase("krijg")) {
            ItemStack readerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

            SkullMeta headMeta = (SkullMeta) readerItem.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            String encodedData = Base64.getEncoder().encodeToString(String.format("{textures:{SKIN:{url:\"%s\"}}}", SKULL_URL).getBytes());
            profile.getProperties().put("textures", new Property("textures", encodedData));

            Field profileField = null;
            try {
                profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

            headMeta.setDisplayName(ChatColor.DARK_AQUA + "Keycard Reader");

            readerItem.setItemMeta(headMeta);

            player.getInventory().addItem(readerItem);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0].toLowerCase(), Collections.singletonList("krijg"), new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
