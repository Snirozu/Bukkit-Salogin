package snirozu.salogin.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import snirozu.salogin.BaseCommand;
import snirozu.salogin.Salogin;
import snirozu.salogin.data.Data;
import snirozu.salogin.data.Database;
import snirozu.salogin.data.Lang;

public class Register implements BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Lang.send(sender, "command-for-players");
            return true;
        }

        Player player = (Player) sender;

        if (Database.isUserRegistered(player.getUniqueId())) {
            Lang.send(sender, "already-registered");
            return true;
        }

        if (args.length > 1) {
            if (args[0].equals(args[1])) {
                if (args[0].length() < Salogin.instance.getConfig().getInt("min-chars-in-password")) {
                    Lang.send(sender, "short-password", Salogin.instance.getConfig().getInt("min-chars-in-password"));
                    return true;
                }
                switch (Database.register(player, args[0])) {
                    case RUN:
                        Data.setLoggedIn(player, true);
                        Lang.send(sender, "on-register");
                        break;
                    case EXCEPTION:
                        Lang.send(sender, "unknown-error-register");
                        break;
                    case CANCELLED:
                        break;
                }
                return true;
            }
            Lang.send(sender, "not-same-password");
            return true;
        }
        Lang.send(sender, "register-correct-syntax", label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of("");
    }
    
}
