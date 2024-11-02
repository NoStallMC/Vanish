package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VanishNotifier {

    public void notifyVanished(Player player) {
        String nickname = player.getDisplayName();
        Bukkit.broadcastMessage(nickname + " left the game.");
    }

    public void notifyUnvanished(Player player) {
        String nickname = player.getDisplayName();
        Bukkit.broadcastMessage(nickname + " joined the game.");
    }
}
