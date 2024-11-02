package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.earth2me.essentials.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MOTDManager {
    private final File motdFile;
    private List<String> motdLines;
    private final Logger logger;
    private final Vanish plugin;

    public MOTDManager(Vanish plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.motdFile = new File("plugins/Vanish/motd.txt");
        loadMOTD();
    }

    private void loadMOTD() {
        motdLines = new ArrayList<>();
        if (!motdFile.exists()) {
            createDefaultMOTD();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(motdFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("  - ")) {
                    motdLines.add(line.substring(4).trim()); // Remove the "  - " prefix
                }
            }
        } catch (IOException e) {
            logger.warning("Could not read MOTD file: " + e.getMessage());
        }
    }

    private void createDefaultMOTD() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(motdFile))) {
            writer.write("motd:\n");
            writer.write("  - &cWelcome, {PLAYER}&c!\n");
            writer.write("  - &fType &c/help&f for a list of commands.\n");
            writer.write("  - Currently online: {PLAYERLIST}\n");
            logger.info("Default MOTD file created at: " + motdFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warning("Could not create default MOTD file: " + e.getMessage());
        }
    }

    public List<String> getMOTD(Player player) {
        List<String> formattedMOTD = new ArrayList<>();
        String playerName = getNickname(player); // Get the player's nickname
        String playerList = generatePlayerList();

        for (String line : motdLines) {
            String formattedLine = line.replace("{PLAYER}", playerName)
                                       .replace("{PLAYERLIST}", playerList);
            // Translate color codes manually
            formattedLine = translateColorCodes(formattedLine);
            formattedMOTD.add(formattedLine);
        }
        return formattedMOTD;
    }

    private String getNickname(Player player) {
        User user = plugin.getEss().getUser(player);
        return user.getDisplayName(); // Retrieve the nickname from the Essentials user object
    }

    private String generatePlayerList() {
        StringBuilder playerList = new StringBuilder();
        for (Player p : Bukkit.getOnlinePlayers()) {
            // Check if the player is vanished
            if (!plugin.getVanishedPlayers().stream().anyMatch(vanishUser -> vanishUser.getName().equals(p.getName()))) {
                if (playerList.length() > 0) {
                    playerList.append(", ");
                }
                playerList.append(getNickname(p)); // Use the nickname for the player list
            }
        }
        return playerList.toString();
    }

    private String translateColorCodes(String message) {
        return message.replace("&0", ChatColor.BLACK + "")
                      .replace("&1", ChatColor.DARK_BLUE + "")
                      .replace("&2", ChatColor.DARK_GREEN + "")
                      .replace("&3", ChatColor.DARK_AQUA + "")
                      .replace("&4", ChatColor.DARK_RED + "")
                      .replace("&5", ChatColor.DARK_PURPLE + "")
                      .replace("&6", ChatColor.GOLD + "")
                      .replace("&7", ChatColor.GRAY + "")
                      .replace("&8", ChatColor.DARK_GRAY + "")
                      .replace("&9", ChatColor.BLUE + "")
                      .replace("&a", ChatColor.GREEN + "")
                      .replace("&b", ChatColor.AQUA + "")
                      .replace("&c", ChatColor.RED + "")
                      .replace("&d", ChatColor.LIGHT_PURPLE + "")
                      .replace("&e", ChatColor.YELLOW + "")
                      .replace("&f", ChatColor.WHITE + "");
    }
}
