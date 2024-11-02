package main.java.org.matejko.plugin;

import com.earth2me.essentials.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Vanish extends JavaPlugin implements Listener {

    private Logger logger;
    private Set<VanishUser> vanishedPlayers;
    private VanishedPlayersManager vanishedPlayersManager;
    private VanishNotifier vanishNotifier;
    private Plugin dynmapPlugin;
    private IEssentials ess;
    private ListCommand listCommand;
    private DynmapManager dynmapManager;
    private MOTDManager motdManager;

    @Override
    public void onEnable() {
        this.logger = Logger.getLogger("Vanish");

        // Ensure the directory exists
        File dataFolder = new File(getDataFolder(), "VanishedPlayers.txt");
        if (!dataFolder.getParentFile().exists()) {
            dataFolder.getParentFile().mkdirs();
        }

        vanishedPlayers = new HashSet<>();
        this.getCommand("vanish").setExecutor(this);
        this.getCommand("v").setExecutor(this);
        this.getCommand("list").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        vanishedPlayersManager = new VanishedPlayersManager(logger);
        vanishedPlayersManager.loadVanishedPlayers(vanishedPlayers);
        logger.info("Vanish Plugin is now active!");

        dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");
        Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentialsPlugin instanceof IEssentials) {
            this.ess = (IEssentials) essentialsPlugin;
        }

        listCommand = new ListCommand(this);
        dynmapManager = new DynmapManager(dynmapPlugin, logger);
        vanishNotifier = new VanishNotifier();
        motdManager = new MOTDManager(this, logger); // Pass the plugin instance
    }

    @Override
    public void onDisable() {
        vanishedPlayersManager.saveVanishedPlayers(vanishedPlayers);
        this.logger.info("Vanish Plugin is now disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("vanish") || cmd.getName().equalsIgnoreCase("v")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!player.isOp()) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }

                if (args.length > 0 && args[0].equalsIgnoreCase("on")) {
                    vanish(player);
                } else if (args.length > 0 && args[0].equalsIgnoreCase("off")) {
                    unVanish(player);
                } else {
                    if (isPlayerVanished(player)) {
                        unVanish(player);
                    } else {
                        vanish(player);
                    }
                }
                return true;
            } else {
                sender.sendMessage("This command can only be executed by a player.");
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("list")) {
            listCommand.execute(sender);
            return true;
        }
        return false;
    }

    private void vanish(Player player) {
        if (!isPlayerVanished(player)) {
            VanishUser vanishUser = new VanishUser(player, true);
            vanishedPlayers.add(vanishUser);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != player) {
                    p.hidePlayer(player);
                }
            }
            player.sendMessage("You are now invisible!");
            logger.info(player.getName() + " has vanished. Total vanished players: " + vanishedPlayers.size());

            dynmapManager.addToHiddenPlayersFile(player.getName());
            vanishNotifier.notifyVanished(player);
        }
    }

    private void unVanish(Player player) {
        VanishUser toRemove = null;
        for (VanishUser vanishUser : vanishedPlayers) {
            if (vanishUser.getPlayer().equals(player)) {
                toRemove = vanishUser;
                break;
            }
        }

        if (toRemove != null) {
            vanishedPlayers.remove(toRemove);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
            player.sendMessage("You are now visible!");
            logger.info(player.getName() + " is now visible. Total vanished players: " + vanishedPlayers.size());

            dynmapManager.removeFromHiddenPlayersFile(player.getName());
            vanishNotifier.notifyUnvanished(player);
        }
    }

    private boolean isPlayerVanished(Player player) {
        for (VanishUser vanishUser : vanishedPlayers) {
            if (vanishUser.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();

        // Suppress the join message for vanished players
        if (isPlayerVanished(newPlayer)) {
            event.setJoinMessage(null); // Disable the join message
        } else {
            // Set a custom join message using the nickname
            String nickname = getNickname(newPlayer);
            event.setJoinMessage(nickname + " joined the game.");
        }

        // Send the MOTD to the new player
        List<String> motd = motdManager.getMOTD(newPlayer);
        for (String line : motd) {
            newPlayer.sendMessage(line);
        }

        // Hide the vanished players from the new player
        for (VanishUser vanishUser : vanishedPlayers) {
            newPlayer.hidePlayer(vanishUser.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quittingPlayer = event.getPlayer();

        // Suppress the leave message for vanished players
        if (isPlayerVanished(quittingPlayer)) {
            event.setQuitMessage(null); // Disable the quit message
        } else {
            // Set a custom leave message using the nickname
            String nickname = getNickname(quittingPlayer);
            event.setQuitMessage(nickname + " left the game.");
        }
    }

    private String getNickname(Player player) {
        // Assuming you have an IEssentials instance
        return ess.getUser(player).getDisplayName(); // Use the nickname from Essentials
    }

    public IEssentials getEss() {
        return ess;
    }

    public Set<VanishUser> getVanishedPlayers() {
        return vanishedPlayers;
    }
}
