package snirozu.salogin.commands;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import snirozu.salogin.BaseCommand;
import snirozu.salogin.Salogin;
import snirozu.salogin.data.Data;
import snirozu.salogin.data.Lang;

public class SaLoginAdmin implements BaseCommand {
    private List<String> argsList = List.of(
        "reload", "setLoginCode", "generateLoginCode", "setLoginSpawn", "updateLang"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "reload":
                    Salogin.instance.reloadConfig();
                    Lang.updateConfig(false);
                    Lang.initLangConfig();
                    Lang.send(sender, "on-config-reload");
                    return true;
                case "setLoginCode", "generateLoginCode":
                    if (args.length == 2) {
                        if (Bukkit.getPlayer(args[1]) == null) {
                            Lang.send(sender, "must-player-online");
                            return true;
                        }
                        
                        Data.usersResetCode.put(Bukkit.getPlayer(args[1]).getUniqueId(), args[2]);
                        Lang.send(sender, "on-set-login-code", args[2], args[1]);
                        return true;
                    }
                    if (args.length >= 2) {
                        if (Bukkit.getPlayer(args[1]) == null) {
                            Lang.send(sender, "must-player-online");
                            return true;
                        }
                        String CODE = args.length > 2 ? args[2] : RandomStringUtils.randomNumeric(7);
                        Data.usersResetCode.put(Bukkit.getPlayer(args[1]).getUniqueId(), CODE);
                        Lang.send(sender, "on-set-login-code", CODE, args[1]);
                        return true;
                    }
                    Lang.send(sender, "setlogincode-correct-syntax", label, args[0]);
                    return true;
                case "setLoginSpawn":
                    if (!(sender instanceof Player)) {
                        Lang.send(sender, "command-for-players");
                        return true;
                    }
                    Player player = (Player) sender;
                    Salogin.instance.getConfig().set("spawn-location.world", player.getWorld().getName());
                    Salogin.instance.getConfig().set("spawn-location.x", player.getLocation().getX());
                    Salogin.instance.getConfig().set("spawn-location.y", player.getLocation().getY());
                    Salogin.instance.getConfig().set("spawn-location.z", player.getLocation().getZ());
                    Salogin.instance.saveConfig();
                    Lang.send(sender, "on-setloginspawn");
                    return true;
                case "updateLang":
                    Lang.updateConfig(true);
                    Lang.send(sender, "on-updatelang");
                    return true;
            }
        }
        Lang.send(sender, "saloginadmin-correct-syntax", label, argsList.toString());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1:
                return argsList;
            case 2:
                if (args[0].equals("setLoginCode")) {
                    // returns players
                    return null;
                }
        }
        return List.of("");
    }
    
}
