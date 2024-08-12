package snirozu.salogin.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.password4j.Hash;
import com.password4j.Password;

import snirozu.salogin.Salogin;
import snirozu.salogin.events.PlayerRegisterEvent;

public class Database {
    // unused but can be useful to someone idk
    public static Integer countTheSameIPsExcluding(String ip, UUID uuid) {
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();) {
            ResultSet results = stmt
            .executeQuery("SELECT COUNT('%s') AS ips FROM users WHERE ip = '%s' AND NOT uuid = '%s'"
            .formatted(ip, ip, uuid.toString()));
            Integer result = results.getInt("ips");
            conn.close();
            stmt.close();
            return result;
        } catch (SQLException exc) {
            Salogin.instance.getLogger().log(Level.SEVERE, "Encountered a exception", exc);
        }
        return null;
    }

    /**
     * @param uuid
     * @param key
     * @return returns null if string is null
     */
    public static String getStringFromUser(UUID uuid, String key) {
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();) {
            ResultSet results = stmt.executeQuery("SELECT * FROM users WHERE uuid = '" + uuid.toString() + "'");

            if (results.next()) {
                String value = results.getString(key);
                conn.close();
                stmt.close();
                return value;
            }
            return null;
        }
        catch (SQLException exc) {
            Salogin.instance.getLogger().log(Level.SEVERE, "Encountered a exception: " + uuid, exc);
        }
        return null;
    }

    /**
     * @param uuid
     * @param key
     * @param content
     * @return `true` when the operation was successfull
     */
    public static boolean setStringInUser(UUID uuid, String key, String content) {
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();) {
            stmt.executeUpdate("UPDATE users SET " + key + " = '" + content + "' WHERE uuid = '" + uuid.toString() + "'");
            conn.close();
            stmt.close();
            return true;
        } catch (SQLException exc) {
            Salogin.instance.getLogger().log(Level.SEVERE, "Encountered a exception!", exc);
        }
        return false;
    }

    /**
     * Registers player and password to the database
     * Returns `RegisterReturn.RUN` if the operation was succesful
     **/
    public static RegisterReturn register(Player player, String password) {
        PlayerRegisterEvent authEvent = new PlayerRegisterEvent(player);
        Bukkit.getPluginManager().callEvent(authEvent);
        if (authEvent.isCancelled()) {
            return RegisterReturn.CANCELLED;
        }
        
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();) {
            Hash hash = Password.hash(password).withBcrypt();
            stmt.executeUpdate("INSERT INTO users(uuid, password, email, lastLocation, ip) VALUES " +
                    "('" + player.getUniqueId().toString() + "', '" + hash.getResult() + "', NULL, NULL, '"
                    + player.getAddress().getAddress().getHostAddress() + "')");
            conn.close();
            stmt.close();
            return RegisterReturn.RUN;
        } catch (SQLException exc) {
            Salogin.instance.getLogger().log(Level.SEVERE, "Encountered a exception!", exc);
        }
        return RegisterReturn.EXCEPTION;
    }

    /**
     * Checks if specific UUID exists in database
     **/
    public static boolean isUserRegistered(UUID uuid) {
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();) {
            ResultSet results = stmt.executeQuery("SELECT * FROM users WHERE uuid = '" + uuid.toString() + "'");
            Boolean registered = results.next();
            conn.close();
            stmt.close();
            return registered;
        } catch (SQLException exc) {
            Salogin.instance.getLogger().log(Level.SEVERE, "Encountered a exception: " + uuid, exc);
        }
        return false;
    }

    public static Connection getConnection() throws SQLException {
        String type = Salogin.instance.getConfig().getString("database.type");

        switch (type) {
            case "sqlite":
                return DriverManager.getConnection("jdbc:sqlite://" + Salogin.instance.getDataFolder().getAbsolutePath() + "/Salogin.db");
            case "mysql":
                String host = Salogin.instance.getConfig().getString("database.host");
                Integer port = Salogin.instance.getConfig().getInt("database.port");
                String name = Salogin.instance.getConfig().getString("database.name");
                String user = Salogin.instance.getConfig().getString("database.user");
                String password = Salogin.instance.getConfig().getString("database.password");
                return DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+name+"", user, password);
        }
        throw new SQLException("Invalid database.type in config.yml");
    }

    public static void init() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();) {
            DatabaseMetaData meta = conn.getMetaData();
            Salogin.instance.getLogger().info("Succesfully connected to database (" + meta.getDriverName() + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (uuid TEXT, password TEXT, email TEXT, lastLocation TEXT, ip TEXT)");
        }
        catch (SQLException exc) {
            Salogin.instance.getLogger().log(Level.SEVERE, "Encountered a problem while initializing a SQLite database: " + Salogin.instance.getDataFolder().toPath(), exc);
        }
    }

    public static enum RegisterReturn {
        RUN,
        CANCELLED,
        EXCEPTION;
    }
}
