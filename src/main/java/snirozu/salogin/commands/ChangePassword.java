package snirozu.salogin.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import snirozu.salogin.BaseCommand;
import snirozu.salogin.data.Data;
import snirozu.salogin.data.Database;
import snirozu.salogin.data.Lang;

public class ChangePassword implements BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Lang.send(sender, "command-for-players");
            return true;
        }

        Player player = (Player) sender;

        if (!Database.isUserRegistered(player.getUniqueId())) {
            Lang.send(sender, "must-register");
            return true;
        }

        if (!Data.isLoggedIn(player)) {
            Lang.send(sender, "must-login");
            return true;
        }

        if (args.length > 1) {
            if (args[0].equals(args[1])) {
                if (Data.changePassword(player.getUniqueId(), args[0])) {
                    Lang.send(sender, "changed-password");
                } else {
                    Lang.send(sender, "unknown-error-changepassword");
                }
                return true;
            }
            Lang.send(sender, "not-same-password");
            return true;
        }

        Lang.send(sender, "changepassword-correct-syntax", label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of("");
    }

}