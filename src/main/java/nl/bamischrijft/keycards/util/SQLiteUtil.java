package nl.bamischrijft.keycards.util;

import com.sun.rowset.CachedRowSetImpl;
import nl.bamischrijft.keycards.Main;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class SQLiteUtil {
    public static CompletableFuture<Boolean> update(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + Main.DATABASE_PATH);
                statement = connection.prepareStatement(sql);

                for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);

                statement.executeUpdate();
                return true;
            } catch (SQLException|ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (statement != null) statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try {
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static CompletableFuture<CachedRowSet> query(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + Main.DATABASE_PATH);
                statement = connection.prepareStatement(sql);
                for (int i = 0; i < params.length; i++)
                    statement.setString(i + 1, params[i].toString());

                resultSet = statement.executeQuery();
                CachedRowSet cachedRowSet = new CachedRowSetImpl();
                cachedRowSet.populate(resultSet);

                return cachedRowSet;
            } catch (SQLException | ClassNotFoundException e) {
                return null;
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try {
                    if (statement != null) statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try {
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
