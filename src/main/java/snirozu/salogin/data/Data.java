package snirozu.salogin.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.password4j.Hash;
import com.password4j.Password;

import snirozu.salogin.Salogin;
import snirozu.salogin.events.PlayerAuthEvent;

public class Data {
    private static ArrayList<Player> loggedInUsers = new ArrayList<>();
    public static HashMap<UUID, String> usersResetCode = new HashMap<>();
    public static HashMap<String, ArrayList<Player>> onlinePlayersIP = new HashMap<>();

    /**
     * Checks if specific Player is logged in
     **/
    public static boolean isLoggedIn(Player player) {
        return loggedInUsers.contains(player);
    }

    public static void setLoggedIn(Player player, boolean bool) {
        if (bool) {
            if (!Data.isLoggedIn(player)) {
                PlayerAuthEvent authEvent = new PlayerAuthEvent(player);
                Bukkit.getPluginManager().callEvent(authEvent);
                if (!authEvent.isCancelled()) {
                    Data.loggedInUsers.add(player);
                    if (Salogin.instance.getConfig().getString("join-player-location") != null && Data.getLastLocation(player.getUniqueId()) != null)
                        player.teleport(Data.getLastLocation(player.getUniqueId()));
                }
            }
            return;
        }
        if (Data.isLoggedIn(player))
            Data.loggedInUsers.remove(player);
    }

    /**
     * Returns true if the `password` is the same as in the database
     **/
    public static boolean login(UUID uuid, String password) {
        String str = Database.getStringFromUser(uuid, "password");
        if (str != null)
            return Password.check(password, str).withBcrypt();

        return false;
    }

    /**
     * Changes password of uuid in the database and automatically hashes it
     * Returns true if the operation was succesful
     **/
    public static boolean changePassword(UUID uuid, String password) {
        Hash hash = Password.hash(password).withBcrypt();
        return Database.setStringInUser(uuid, "password", hash.getResult());
    }

    /**
     * Sets email of uuid in the database
     * Returns true if the operation was succesful
     **/
    public static boolean setEmail(UUID uuid, String email) {
        return Database.setStringInUser(uuid, "email", email);
    }

    /**
     * Sets lastlocation of uuid in the database
     * Returns true if the operation was succesful
     **/
    public static boolean setLastLocation(UUID uuid, Location location) {
        return Database.setStringInUser(uuid, "lastLocation", "%s,%s,%s,%s".formatted(location.getWorld().getName(), location.getX(), location.getY(), location.getZ()));
    }

    public static Location getLastLocation(UUID uuid) {
        String loc = Database.getStringFromUser(uuid, "lastLocation");
        if (loc == null) return null;
        String[] strLocation = loc.split(",");
        Location location = new Location(
            Bukkit.getWorld(strLocation[0]), 
            Double.parseDouble(strLocation[1]), 
            Double.parseDouble(strLocation[2]), 
            Double.parseDouble(strLocation[3])
        );
        Player player;
        if ((player = Bukkit.getPlayer(uuid)) != null) {
            location.setDirection(player.getLocation().getDirection());
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());
        }
        return location;
    }
}
