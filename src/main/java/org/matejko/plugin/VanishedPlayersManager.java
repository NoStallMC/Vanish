package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class VanishedPlayersManager {

    private final String FILE_PATH = "plugins/Vanish/VanishedPlayers.txt";
    private Logger logger;

    public VanishedPlayersManager(Logger logger) {
        this.logger = logger;
    }

    public void loadVanishedPlayers(Set<VanishUser> vanishedPlayers) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
            for (String name : lines) {
                Player player = Bukkit.getPlayer(name);
                if (player != null && player.isOnline()) {
                    VanishUser vanishUser = new VanishUser(player, true); // Pass 'true' for isVanished
                    vanishedPlayers.add(vanishUser);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p != player) {
                            p.hidePlayer(player);
                        }
                    }
                }
            }
            logger.info("Loaded vanished players: " + vanishedPlayers.size());
        } catch (IOException e) {
            logger.warning("Could not load vanished players: " + e.getMessage());
        }
    }

    public void saveVanishedPlayers(Set<VanishUser> vanishedPlayers) {
        try {
            List<String> playerNames = new ArrayList<>();
            for (VanishUser vanishUser : vanishedPlayers) {
                playerNames.add(vanishUser.getName());
            }
            Files.write(Paths.get(FILE_PATH), playerNames);
            logger.info("Saved vanished players: " + vanishedPlayers.size());
        } catch (IOException e) {
            logger.warning("Could not save vanished players: " + e.getMessage());
        }
    }
}
