package snirozu.salogin.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import snirozu.salogin.BaseCommand;
import snirozu.salogin.data.Data;
import snirozu.salogin.data.Database;
import snirozu.salogin.data.Lang;

public class Login implements BaseCommand {

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

        if (args.length > 0) {
            if (Data.login(player.getUniqueId(), args[0])) {
                Data.setLoggedIn(player, true);
                Lang.send(sender, "on-login");
                return true;
            }
            Lang.send(sender, "wrong-password");
            return true;
        }
        Lang.send(sender, "login-correct-syntax", label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of("");
    }
    
}
