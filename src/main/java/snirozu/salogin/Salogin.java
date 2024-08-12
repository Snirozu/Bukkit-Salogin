package snirozu.salogin;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.ArrayList;

import snirozu.salogin.commands.ChangePassword;
import snirozu.salogin.commands.ForgotPassword;
import snirozu.salogin.commands.Login;
import snirozu.salogin.commands.LoginCode;
import snirozu.salogin.commands.Register;
import snirozu.salogin.commands.SaLoginAdmin;
import snirozu.salogin.commands.SetEmail;
import snirozu.salogin.data.Database;
import snirozu.salogin.data.Lang;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.spawn.IEssentialsSpawn;

//TODO lang files
//TODO add auto ban for brute forcing to login
//TODO add "ip auto login sessions"
//TODO add cooldowns

public class Salogin extends JavaPlugin {

    public static Salogin instance;

    public static ArrayList<String> filterCommands;
    public static ArrayList<String> preloginCommands;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        Lang.initLangConfig();
        
        Database.init();

        filterCommands = new ArrayList<>();
        preloginCommands = new ArrayList<>();

        getServer().getPluginManager().registerEvents(new EventListener(), this);

        setCommandExecutor(new String[] { "register", "reg" }, new Register(), false, true);
        setCommandExecutor(new String[] { "login", "l" }, new Login(), false, true);
        setCommandExecutor(new String[] { "saloginadmin", "salus" }, new SaLoginAdmin(), true, false);
        setCommandExecutor(new String[] { "changepass", "changepassword" }, new ChangePassword(), false, false);
        setCommandExecutor(new String[] { "forgotpassword" }, new ForgotPassword(), true, true);
        setCommandExecutor(new String[] { "logincode" }, new LoginCode(), false, true);
        setCommandExecutor(new String[] { "setemail" }, new SetEmail(), true, false);

        Logger consoleLogger = (Logger) LogManager.getRootLogger();
        consoleLogger.addFilter(new LoggingFilter());

        //add plugman support UNLIKE other plugins like discordsrv
        for (Player player : getServer().getOnlinePlayers()) {
            Lang.send(player, "plugin-reload-notify");
            EventListener.onJoin(player);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving player data...");
        for (Player player : getServer().getOnlinePlayers()) {
            EventListener.onLeave(player);
        }
    }

    public void setCommandExecutor(String[] names, BaseCommand baseCommand, boolean doLog, boolean isPreLoginCommand) {
        for (String name : names) {
            if (isPreLoginCommand)
                preloginCommands.add("/" + name);
            if (!doLog)
                filterCommands.add("/" + name);
            PluginCommand command = getCommand(name);
            command.setExecutor(baseCommand);
            command.setTabCompleter(baseCommand);
        }
    }

    public Location getLoginLocation(Player player) {
        switch (Salogin.instance.getConfig().getString("join-player-location")) {
            case "essentialsSpawn":
                if (Bukkit.getPluginManager().getPlugin("Essentials") != null && Bukkit.getPluginManager().getPlugin("EssentialsSpawn") != null) {
                    IEssentials ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
                    IEssentialsSpawn essSpawn = (IEssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
                    User user = ess.getUser(player);
                    return essSpawn.getSpawn(user.getGroup());
                } else {
                    Salogin.instance.getLogger().warning("Failed to teleport player to spawn, please install EssentialsX and EssentialsXSpawn");
                }
                break;
            case "salogin":
                if (Salogin.instance.getConfig().getString("spawn-location.world") != null) {
                    String world = Salogin.instance.getConfig().getString("spawn-location.world");
                    Double x = Salogin.instance.getConfig().getDouble("spawn-location.x");
                    Double y = Salogin.instance.getConfig().getDouble("spawn-location.y");
                    Double z = Salogin.instance.getConfig().getDouble("spawn-location.z");
                    Float yaw = (float) Salogin.instance.getConfig().getDouble("spawn-location.yaw");
                    Float pitch = (float) Salogin.instance.getConfig().getDouble("spawn-location.pitch");
                    Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                    loc.setYaw(yaw);
                    loc.setPitch(pitch);
                    return loc;
                } else {
                    Salogin.instance.getLogger().warning("spawn-location.world is null in config.yml, please set login location with /saloginadmin setloginspawn");
                }
                break;
            default:
                return null;
        }
        return null;
    }
    
    // copied from login security lol
    public static class LoggingFilter extends AbstractFilter {
        private Result handle(String message) {
            if (message == null) {
                return Result.NEUTRAL;
            }

            message = message.toLowerCase();
            for (String word : Salogin.filterCommands) {
                if (message.startsWith(word) || message.contains("issued server command: " + word)) {
                    return Result.DENY;
                }
            }

            return Result.NEUTRAL;
        }

        @Override
        public Result filter(LogEvent event) {
            return handle(event.getMessage().getFormattedMessage());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
            return handle(msg.getFormattedMessage());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
            return handle(msg.toString());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
            return handle(msg);
        }
    }
}
