package nl.bamischrijft.keycards;

import nl.bamischrijft.keycards.commands.KeyCardCommand;
import nl.bamischrijft.keycards.commands.KeyCardReaderCommand;
import nl.bamischrijft.keycards.listeners.KeyCardListener;
import nl.bamischrijft.keycards.listeners.KeyCardReaderListener;
import nl.bamischrijft.keycards.managers.KeyCardReaderManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class Main extends JavaPlugin {
    public static String DATABASE_PATH;
    public static KeyCardReaderManager readerManager;
    private static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getDataFolder().mkdirs();
        DATABASE_PATH = getDataFolder().getAbsolutePath() + File.separator + "data.db";
        if (!Files.exists(Paths.get(DATABASE_PATH))) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS keycardreaders(" +
                        "location VARCHAR(50) UNIQUE NOT NULL, " +
                        "placed_against VARCHAR(50) NOT NULL, " +
                        "cards LONGTEXT);");
                statement.execute();
                statement.close();
                Bukkit.getLogger().info("SQLite bestand aangemaakt");
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Er ging iets fout met het creevan de database. De plugin wordt uitgeschakeld.");
                e.printStackTrace();
                setEnabled(false);
            }
        }

        readerManager = new KeyCardReaderManager();

        PluginCommand keyCardCommand = getCommand("keycard");
        keyCardCommand.setExecutor(new KeyCardCommand());
        keyCardCommand.setTabCompleter(new KeyCardCommand());

        PluginCommand keyCardReaderCommand = getCommand("keycardreader");
        keyCardReaderCommand.setExecutor(new KeyCardReaderCommand());
        keyCardReaderCommand.setTabCompleter(new KeyCardReaderCommand());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new KeyCardListener(), this);
        pm.registerEvents(new KeyCardReaderListener(), this);
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static KeyCardReaderManager getReaderManager() {
        return readerManager;
    }
}
