package net.notexmc.freezetag.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.notexmc.freezetag.FreezeTagPlugin;
import net.notexmc.freezetag.game.FreezeTagManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;



public class GameStartListener implements Listener {

    private final FreezeTagPlugin plugin;
    private final int kickDelaySeconds = 10; // Adjust kick delay as needed
    private Player player;

    public GameStartListener(FreezeTagPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        int onlinePlayers = Bukkit.getServer().getOnlinePlayers().size();

        if (onlinePlayers < 4) {
            event.getPlayer().sendMessage("§cWaiting for more players! Minimum 4 players required.");
        } else if (onlinePlayers == 4) {
            event.getPlayer().sendMessage("§aMinimum Requirements Met! Starting Game...");
        } else if (onlinePlayers <= 10) {
            String worldName = event.getPlayer().getWorld().getName();
            String registeredWorld = plugin.getWorldsConfig().getString("worlds");

            if (worldName.equalsIgnoreCase(registeredWorld)) {
                // Start the game for the player
                plugin.getGameManager().startRound(event.getPlayer());

                // Start countdown timer after game finishes
                plugin.getGameManager().setOnGameEnd(() -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Check if game is still finished
                            if (plugin.getGameManager().isGameFinished()) {
                                kickPlayer(event.getPlayer());
                            }
                        }
                    }.runTaskLater(plugin, kickDelaySeconds * 20); // Run task after delay
                });
            }
        }

        // Start countdown timer
        startCountdown(onlinePlayers);
    }

    // private void kickPlayer(Player player) {
    //     player.kickPlayer("The game has finished!");
    // }
    private void kickPlayer(Player player) {
      // Teleport player to lobby instead of kicking
      Location lobbyLocation = getLobbyLocation();
      if (lobbyLocation != null) {
        player.teleport(lobbyLocation);
        player.sendMessage("The game has ended! You've been teleported back to the lobby.");
      } else {
        // Handle case where lobby location is unavailable (inform player)
        player.kickPlayer("The game has ended! However, teleporting to the lobby failed.");
      }
    }

    private Location getLobbyLocation() {
      // Access the lobby location from your plugin (e.g., through FreezeTagPlugin#getLobbyLocation())
      return plugin.getLobbyListener().getLocation(); // Assuming LobbyListener provides a getLocation() method
    }

    public int calculateCountdown(int playerCount) {
        int baseDuration = 60;
        int minPlayers = 4;
        int maxDuration = 30;
        int durationPerPlayer = 5;

        int adjustedDuration = baseDuration - (playerCount - minPlayers) * durationPerPlayer;

        return Math.max(Math.min(adjustedDuration, maxDuration), 15); // Clamp between 15 and maxDuration
    }

    // private void startCountdown(int playerCount) {
    //     int countdownSeconds = calculateCountdown(playerCount);
  
    //     new BukkitRunnable() {
    //         @Override
    //         public void run() {
    //             if (countdownSeconds > 0) {
    //                 // Broadcast remaining time every 5 seconds
    //                 if (countdownSeconds % 5 == 0) {
    //                     Bukkit.getServer().broadcastMessage("§aGame starting in " + countdownSeconds + " seconds!");
    //                 }
    //                 countdownSeconds--;
    //             } else {
    //                 // Start the game after countdown ends
    //                 FreezeTagManager.startGameAll();
    //                 cancel();
    //             }
    //         }
    //     }.runTaskTimer(plugin, 20L, 20L); // Run every 20 ticks (1 second)
    // }

    // private void startCountdown(int playerCount) {
    //     AtomicInteger countdownSeconds = new AtomicInteger(calculateCountdown(playerCount));

    //     new BukkitRunnable() {
    //         @Override
    //         public void run() {
    //             int seconds = countdownSeconds.get();
    //             if (seconds > 0) {
    //                 // Broadcast remaining time every 5 seconds
    //                 if (seconds % 5 == 0) {
    //                     Bukkit.getServer().broadcastMessage("§aGame starting in " + seconds + " seconds!");
    //                 }
    //                 countdownSeconds.decrementAndGet();
    //             } else {
    //                 // Start the game after countdown ends
    //                 FreezeTagManager.startGameAll();
    //                 cancel();
    //             }
    //         }
    //     }.runTaskTimer(plugin, 20L, 20L); // Run every 20 ticks (1 second)
    // }

    private void startCountdown(int playerCount) {
      AtomicInteger countdownSeconds = new AtomicInteger(calculateCountdown(playerCount));
      new BukkitRunnable() {
        @Override
        public void run() {
          int seconds = countdownSeconds.get();
          if (seconds > 0) {
            // Broadcast remaining time...
          } else {
            // Start the game after countdown ends
            FreezeTagManager.startGameAll();
            // Set action to be executed on game end
            if (plugin.getGameManager().isGameFinished()) {
              plugin.getServer().getScheduler().runTaskLater(plugin, () -> kickPlayer(player), kickDelaySeconds * 20L);
            }// Call this runnable again
            cancel();
          }
        }
      }.runTaskTimer(plugin, 20L, 20L);
    }

}
