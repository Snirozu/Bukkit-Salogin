package snirozu.salogin.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import snirozu.salogin.BaseCommand;
import snirozu.salogin.data.Data;
import snirozu.salogin.data.Database;
import snirozu.salogin.data.Lang;

public class LoginCode implements BaseCommand {

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

        if (Data.isLoggedIn(player)) {
            Lang.send(sender, "already-logged-in");
            return true;
        }

        if (!Data.usersResetCode.containsKey(player.getUniqueId())) {
            Lang.send(sender, "no-login-code-found");
            return true;
        }

        if (args.length > 0) {
            if (Data.usersResetCode.get(player.getUniqueId()).equals(args[0])) {
                Data.setLoggedIn(player, true);
                Data.usersResetCode.remove(player.getUniqueId());
                Lang.send(sender, "on-login-code");
                return true;
            }
            Data.usersResetCode.remove(player.getUniqueId());
            Lang.send(sender, "wrong-login-code");
            return true;
        }
        Lang.send(sender, "login-code-correct-syntax", label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of("");
    }

}
