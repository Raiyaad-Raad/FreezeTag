package net.notexmc.freezetag.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import java.util.Arrays;


import net.notexmc.freezetag.game.Role;
import net.notexmc.freezetag.game.FreezeTagPlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import net.notexmc.freezetag.game.PlayerSpectator;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import java.util.Collection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;



import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;


import net.notexmc.freezetag.FreezeTagPlugin;

public class FreezeTagManager {

    private static FreezeTagPlugin plugin;
    private final Map<UUID, FreezeTagPlayer> players;
    private final List<FreezeTagPlayer> icePlayers;
    private final List<FreezeTagPlayer> waterPlayers;
    private int round;
    private long roundStartTime;
    private boolean gameFinished;
    private Runnable onGameEnd;
    private final Map<UUID, PlayerSpectator> spectators;
    private final ScoreboardManager scoreboardManager;
    private final Map<UUID, Objective> playerScores;
    private Objective objective;
    private Scoreboard scoreboard;
    private long gameStartTime;
    private Map<UUID, Double> playerExp;

    public void setOnGameEnd(Runnable runnable) {
        this.onGameEnd = runnable;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public FreezeTagManager(FreezeTagPlugin plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
        this.spectators = new HashMap<>();
        this.icePlayers = new ArrayList<>();
        this.waterPlayers = new ArrayList<>();  
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.playerScores = new HashMap<>();
        this.gameStartTime = 0;
        this.playerExp = new HashMap<>();
    }
  
    public static void setPlugin(FreezeTagPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public void startRound(Player startingPlayer) {
        // Assign roles based on player count

        if (startingPlayer != null) {
          plugin.getServer().getOnlinePlayers();
        }
        icePlayers.clear();
        waterPlayers.clear();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            createScoreboard(player);
          
            Role role = Role.WATER;
            if (icePlayers.size() < 1) {
                role = Role.ICE;
            }
            FreezeTagPlayer newPlayer = new FreezeTagPlayer(player, role);
            players.put(player.getUniqueId(), newPlayer);
            if (role == Role.ICE) {
                icePlayers.add(newPlayer);
            } else {
                waterPlayers.add(newPlayer);
            }
        }
        roundStartTime = System.currentTimeMillis();
        gameStartTime = System.currentTimeMillis(); // Start time for XP calculation
        playerExp.clear(); // Reset player XP for new round
        plugin.getServer().broadcastMessage("§aFreezeTag round " + (round + 1) + " has started!");

        // Enable player movement, activate Ice freezing ability
        for (FreezeTagPlayer player : players.values()) {
            if (player.getRole() == Role.WATER) {
                player.unfreeze(); // Ensure water players start unfrozen
            }
            player.getPlayer().setAllowFlight(true); // Enable player movement
        }
    }

    public void onPlayerJoin(Player player) {
        playerExp.put(player.getUniqueId(), 0.0); // Add new player to XP map
        player.sendMessage("§a**Waiting for enough players to start the game!**");
    }

    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameStartTime > 0) { // Game is running
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            // Calculate earned XP based on time since game start (capped at 0.5 per second)
            double currentExp = playerExp.getOrDefault(uuid, 0.0);
            double timeSinceStart = (System.currentTimeMillis() - gameStartTime) / 1000.0;
            double earnedExp = Math.min(timeSinceStart / 2.0, 0.5);

            playerExp.put(uuid, currentExp + earnedExp);
        }
    }

    public void onPlayerFreeze(Player frozenPlayer) {
        if (gameStartTime > 0) { // Game is running
            UUID uuid = frozenPlayer.getUniqueId();
            double currentExp = playerExp.getOrDefault(uuid, 0.0);

            // Award freeze bonus (0.2 EXP instead of regular 0.5 EXP earned while moving)
            playerExp.put(uuid, currentExp + 0.2);
        }
    }

    public void onPlayerUnfreeze(Player unfrozenPlayer) {
        if (gameStartTime > 0) // Game is running
        {
            UUID uuid = unfrozenPlayer.getUniqueId();
            double currentExp = playerExp.getOrDefault(uuid, 0.0);

            // Award unfreeze bonus (back to regular 0.5 EXP earned while moving)
            playerExp.put(uuid, currentExp + 0.5);
        }
    }

    public void onIcePlayerAction(Player icePlayer) {
        if (gameStartTime > 0) { // Game is running
            UUID uuid = icePlayer.getUniqueId();
            double currentExp = playerExp.getOrDefault(uuid, 0.0);

            // Award higher XP for ice player actions (0.7 EXP) to encourage active play
            playerExp.put(uuid, currentExp + 0.7);
        }
    }

    public void onFreezeSuccess(Player icePlayer, Player frozenPlayer) {
        if (gameStartTime > 0) { // Game is running
            UUID icePlayerUuid = icePlayer.getUniqueId();
            double icePlayerExp = playerExp.getOrDefault(icePlayerUuid, 0.0);

            // Award bonus XP to ice player for successfully freezing another player (2.5 EXP)
            playerExp.put(icePlayerUuid, icePlayerExp + 2.5);
        }
    }

    public void checkRoundEnd() {
        if (isTimeUp()) {
            if (allWaterFrozen()) {
                // Water wins
                plugin.getLogger().info("Water wins!");
                announceWinner(Role.WATER);
                startNewRound();
            } else {
                // Ice wins
                plugin.getLogger().info("Ice wins!");
                announceWinner(Role.ICE);
                endGame();
            }
        }
    }

    private boolean isTimeUp() {
        return System.currentTimeMillis() - roundStartTime >= 4 * 60 * 1000; // 4 minutes
    }

    private boolean allWaterFrozen() {
        for (FreezeTagPlayer player : waterPlayers) {
            if (!player.isFrozen()) {
                return false;
            }
        }
        return true;
    }

    private void announceWinner(Role winner) {
        plugin.getServer().broadcastMessage("§a" + winner + " team wins this round!");
    }

    private void startNewRound() {
        round++;
        startRound(null); // Reset and start a new round
    }

    private void endGame() {
        gameFinished = true;

        if (onGameEnd != null) {
            onGameEnd.run();
        }
        // for (Player player : Bukkit.getOnlinePlayers()) {
            // updateScoreboard(player);
        // }
        // End the game, send players to lobby (optional)
        plugin.getServer().broadcastMessage("§cThe game has ended!");
        for (FreezeTagPlayer player : players.values()) {
            player.getPlayer().setAllowFlight(false); // Disable player movement
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    public void onRoundEnd(Role winningRole) {
            if (winningRole == Role.WATER) {
                // Water team wins (award 10 EXP)
                for (FreezeTagPlayer player : players.values()) {
                    if (player.getRole() == Role.WATER) {
                        awardExp(player.getPlayer(), 10.0);
                    }
                }
                plugin.getServer().broadcastMessage("§a**Water team wins!**");
            } else if (winningRole == Role.ICE) {
                // Ice team wins (award 15 EXP)
                for (FreezeTagPlayer player : players.values()) {
                    if (player.getRole() == Role.ICE) {
                        awardExp(player.getPlayer(), 15.0); // Typo fixed
                    }
                }
                plugin.getServer().broadcastMessage("§a**Ice team wins!**");
            } else {
                // Losing team consolation prize (award 5 EXP)
                for (FreezeTagPlayer player : players.values()) {
                    awardExp(player.getPlayer(), 5.0);
                }
                plugin.getServer().broadcastMessage("§a**The game is a draw!**");
            }
        }

    private void awardExp(Player player, double exp) {
        UUID uuid = player.getUniqueId();
        double currentExp = getPlayerExp(uuid);
        double newExp = currentExp + exp;
  
        playerExp.put(uuid, newExp); // Update in-memory map
        updatePlayerExpInFile(uuid, newExp); // Update player.yml
  
        player.sendMessage("§aYou earned " + exp + " EXP!");
    }

    private double getPlayerExp(UUID uuid) {
        if (playerExp.containsKey(uuid)) {
            return playerExp.get(uuid);
        } else {
            return 0.0; // Default for new players
        }
    }

    private void updatePlayerExpInFile(UUID uuid, double newExp) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(plugin.getPlayersFile());
        config.set(uuid.toString() + ".exp", newExp);

        try {
            config.save(plugin.getPlayersFile());
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving players.yml file!");
            e.printStackTrace();
        }
    }
    
    public FreezeTagPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }
  
    public void makeSpectator(Player player) {
        if (player.isFrozen()) {
            // Remove player from game and add to spectators
            FreezeTagPlayer frozenPlayer = players.remove(player.getUniqueId());
            if (frozenPlayer != null) {
                PlayerSpectator spectator = new PlayerSpectator(player, plugin);
                spectators.put(player.getUniqueId(), spectator);
                spectator.startSpectating(); // Implement logic to enable spectating
                spectator.playFreezeSound(); // Play freeze sound
            }
        } else {
            // Player is not frozen, cannot spectate
            player.sendMessage("§cYou need to be frozen to spectate!");
        }
    }

  
    private void createScoreboard(Player player) {
        Objective teamScores = scoreboard.registerNewObjective("teamscores", "dummy", "Teams");
        teamScores.setDisplaySlot(DisplaySlot.BELOW_NAME);
        player.setScoreboard(scoreboard);
        playerScores.put(player.getUniqueId(), teamScores);
    }
    
    private void startScoreboardUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);  // Run every 20 ticks (1 second)
    }

    private void updateScoreboard(Player player) {
        Objective objective = playerScores.get(player.getUniqueId());
        if (objective != null) {
            int secondsRemaining = Math.round((System.currentTimeMillis() - roundStartTime) / 1000);
            objective.getScore("§7Time:").setScore(secondsRemaining);
        }
    }

    public static void startGameAll() {
        // Announce game start and initiate countdown
        plugin.getServer().broadcastMessage("§aFreezeTag game starting in 5 seconds!");
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {
                    plugin.getServer().broadcastMessage("§aStarting in " + countdown + " seconds...");
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    }
                    countdown--;
                } else {
                    // Start the round
                    FreezeTagManager manager = new FreezeTagManager(plugin);
                    manager.startRound(null);
                    manager.startScoreboardUpdater(); // Start updating scoreboards
                    cancel(); // Stop the countdown task
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }




    // Additional methods for managing game events, logic, etc.
}
