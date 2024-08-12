package snirozu.salogin.commands;

import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import snirozu.salogin.BaseCommand;
import snirozu.salogin.Salogin;
import snirozu.salogin.SendEmail;
import snirozu.salogin.data.Data;
import snirozu.salogin.data.Database;
import snirozu.salogin.data.Lang;

public class ForgotPassword implements BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Lang.send(sender, "command-for-players");
            return true;
        }

        Player player = (Player) sender;

        if (!Salogin.instance.getConfig().getBoolean("email.enabled")) {
            Lang.send(sender, "emails-disabled");
            return true;
        }

        if (Database.getStringFromUser(player.getUniqueId(), "email") == null) {
            Lang.send(sender, "not-set-email");
            return true;
        }

        if (!Database.isUserRegistered(player.getUniqueId())) {
            Lang.send(sender, "must-register");
            return true;
        }

        if (Data.isLoggedIn(player)) {
            Lang.send(sender, "forgotpassword-logged-in");
            return true;
        }

        String code = RandomStringUtils.randomNumeric(7);
        Data.usersResetCode.put(player.getUniqueId(), code);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                SendEmail.sendEmail(Database.getStringFromUser(player.getUniqueId(), "email"), Lang.local("forgotpassword-email-subject"), Lang.local("forgotpassword-email-content", code));
                Lang.send(sender, "forgotpassword-sent-code");
            }
        });
        Lang.send(sender, "forgotpassword-sending-code");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of("");
    }
    
}
