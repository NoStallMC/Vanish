package main.java.org.matejko.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.earth2me.essentials.User;

import java.util.*;

public class ListCommand {

    private final Vanish plugin;

    public ListCommand(Vanish plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender) {
        boolean showHidden = true; // Adjust based on your settings
        int playerHidden = 0;
        StringBuilder online = new StringBuilder();

        // Count hidden players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getVanishedPlayers().stream().anyMatch(vanishUser -> vanishUser.getName().equals(player.getName()))) {
                playerHidden++;
            }
        }

        online.append(ChatColor.BLUE).append("There are ")
              .append(ChatColor.RED).append(Bukkit.getOnlinePlayers().length - playerHidden)
              .append(ChatColor.BLUE).append(" out of a maximum ")
              .append(ChatColor.RED).append(Bukkit.getMaxPlayers())
              .append(ChatColor.BLUE).append(" players online.");

        sender.sendMessage(online.toString());

        // If sorting by groups is enabled
        if (plugin.getEss().getSettings().getSortListByGroups()) {
            Map<String, List<User>> sort = new HashMap<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                User u = plugin.getEss().getUser(p);
                if (!u.isHidden() || showHidden) {
                    String group = u.getGroup();
                    sort.computeIfAbsent(group, k -> new ArrayList<>()).add(u);
                }
            }

            // Sort and display groups
            String[] groups = sort.keySet().toArray(new String[0]);
            Arrays.sort(groups, String.CASE_INSENSITIVE_ORDER);
            for (String group : groups) {
                List<User> users = sort.get(group);
                Collections.sort(users);
                StringBuilder groupString = new StringBuilder(ChatColor.GOLD + group + ": ");

                boolean first = true;
                for (User user : users) {
                    if (!first) {
                        groupString.append(", ");
                    } else {
                        first = false;
                    }
                    if (user.isAfk()) {
                        groupString.append(ChatColor.YELLOW).append("[AFK] ");
                    }
                    if (user.isHidden()) {
                        groupString.append(ChatColor.GRAY).append("[HIDDEN] ");
                    }
                    groupString.append(user.getDisplayName());
                }
                sender.sendMessage(groupString.toString());
            }
        } else {
            List<User> users = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                User u = plugin.getEss().getUser(p);
                if (plugin.getVanishedPlayers().stream().noneMatch(vanishUser -> vanishUser.getName().equals(p.getName())) &&
                    (!u.isHidden() || showHidden)) {
                    users.add(u);
                }
            }
            Collections.sort(users);
            StringBuilder onlineUsers = new StringBuilder("Connected Players: ");
            
            boolean first = true;
            for (User user : users) {
                if (!first) {
                    onlineUsers.append(", ");
                } else {
                    first = false;
                }
                if (user.isAfk()) {
                    onlineUsers.append(ChatColor.GRAY).append("[AFK] ");
                }
                if (user.isHidden()) {
                    onlineUsers.append(ChatColor.GRAY).append("[HIDDEN] ");
                }
                onlineUsers.append(user.getDisplayName());
            }
            sender.sendMessage(onlineUsers.toString());
        }
    }
}
