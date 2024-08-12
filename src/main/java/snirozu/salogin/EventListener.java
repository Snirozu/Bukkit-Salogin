package snirozu.salogin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import snirozu.salogin.data.Data;
import snirozu.salogin.data.Database;
import snirozu.salogin.data.Lang;

import org.bukkit.event.block.*;

public class EventListener implements Listener {
    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        EventListener.onLeave(e.getPlayer());
    }

    public static void onLeave(Player player) {
        String address = player.getAddress().getAddress().getHostAddress();
        if (Data.onlinePlayersIP.containsKey(address)) {
            Data.onlinePlayersIP.get(address).remove(player);
        }

        if (Data.isLoggedIn(player)) {
            Data.setLastLocation(player.getUniqueId(), player.getLocation());
        }
        Data.setLoggedIn(player, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if ((Data.onlinePlayersIP.get(event.getAddress().getHostAddress()) == null ? 0 : Data.onlinePlayersIP.get(event.getAddress().getHostAddress()).size()) >= Salogin.instance.getConfig().getInt("player-per-ip-limit")) {
            event.disallow(Result.KICK_BANNED, Lang.local("join-limit-ip"));
        }
    }

    private static HashMap<Player, Integer> logRegTasks = new HashMap<>();

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        EventListener.onJoin(e.getPlayer());
    }

    public static void onJoin(Player player) {
        String address = player.getAddress().getAddress().getHostAddress();
        if (!Data.onlinePlayersIP.containsKey(address)) {
            Data.onlinePlayersIP.put(address, new ArrayList<>());
        }
        Data.onlinePlayersIP.get(address).add(player);

        if (Salogin.instance.getConfig().getString("join-player-location") != null) {
            player.teleport(Salogin.instance.getLoginLocation(player));
        }
        
        if (logRegTasks.containsKey(player))
            Bukkit.getScheduler().cancelTask(logRegTasks.get(player));

        logRegTasks.put(player, 
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Salogin.instance, new Runnable() {
                int timesRan = 0;

                @Override
                public void run() {
                    if (Data.isLoggedIn(player)) {
                        Bukkit.getScheduler().cancelTask(logRegTasks.remove(player));
                        return;
                    }
                    
                    if (Salogin.instance.getConfig().getInt("login-notify-period") > 0 && ++timesRan >= Salogin.instance.getConfig().getInt("login-notify-period")) {
                        player.kickPlayer( (Database.isUserRegistered(player.getUniqueId()) ? Lang.local("login-too-long") : Lang.local("register-too-long")));
                        return;
                    }

                    if (Database.isUserRegistered(player.getUniqueId())) {
                        Lang.send(player, "login-notify");
                        return;
                    }
                    Lang.send(player, "register-notify");
                }
            }, 20 * Salogin.instance.getConfig().getInt("login-notify-delay"), 20 * Salogin.instance.getConfig().getInt("login-notify-delay"))
        );
    }

    // ====================================================================
    // CANCELLABLE EVENTS FOR NON-LOGGED-IN PLAYERS
    // ====================================================================
    public void qCancelEvent(Cancellable event, @Nonnull String getPlayerFuncName) {
        try {
            Class<?> eventClass = Class.forName(event.getClass().getName());
            Object entity = eventClass.getMethod(getPlayerFuncName).invoke(event);
            if (!(entity instanceof Player))
                return;
            
            if (!Data.isLoggedIn((Player) entity)) {
                Method setCancelled = eventClass.getMethod("setCancelled", boolean.class);
                setCancelled.invoke(event, true);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void qCancelEvent(Cancellable event) {
        qCancelEvent(event, "getPlayer");
    }

    @EventHandler
    private void onCancellableEvent(PlayerMoveEvent e) {
        if (!Data.isLoggedIn(e.getPlayer()) && (e.getTo().getBlock().getX() != e.getFrom().getBlock().getX() || e.getTo().getBlock().getZ() != e.getFrom().getBlock().getZ() || e.getTo().getY() > e.getFrom().getY())) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    private void onCancellableEvent(PlayerCommandPreprocessEvent e) {
        // not using startswith because it can cause exploits
        String command = e.getMessage().split(" ")[0];
        if (command.startsWith("/salogin:")) command = "/" + command.substring("/salogin:".length());
        if (Salogin.preloginCommands.contains(command))
            return;
        qCancelEvent(e);
    };
    @EventHandler private void onCancellableEvent(AsyncPlayerChatEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(BlockBreakEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(BlockDamageEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(BlockMultiPlaceEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(BlockPlaceEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerArmorStandManipulateEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerBedEnterEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerBucketEmptyEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerBucketFillEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerDropItemEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerEditBookEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerFishEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerGameModeChangeEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerInteractAtEntityEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerInteractEntityEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerInteractEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerItemConsumeEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerItemDamageEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerItemHeldEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerLeashEntityEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerPickupArrowEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerShearEntityEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerSwapHandItemsEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerToggleFlightEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerToggleSneakEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerToggleSprintEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(PlayerUnleashEntityEvent e) { qCancelEvent(e); };
    @EventHandler private void onCancellableEvent(InventoryOpenEvent e) {qCancelEvent(e);}
    @EventHandler private void onCancellableEvent(InventoryClickEvent e) {qCancelEvent(e, "getWhoClicked");}
    @EventHandler private void onCancellableEvent(EntityDamageEvent e) { qCancelEvent(e, "getEntity"); }
    @EventHandler private void onCancellableEvent(EntityDamageByEntityEvent e) {qCancelEvent(e, "getEntity");}
    @EventHandler private void onCancellableEvent(SignChangeEvent e) {qCancelEvent(e);}

    //CANCEL FOR OLD VERSIONS
    @SuppressWarnings("deprecation") @EventHandler private void onCancellableOldEvent(PlayerPickupItemEvent e) {qCancelEvent(e);}
    @SuppressWarnings("deprecation") @EventHandler private void onCancellableOldEvent(PlayerChatEvent e) {qCancelEvent(e);}
}
