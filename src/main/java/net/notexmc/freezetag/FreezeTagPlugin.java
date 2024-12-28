package net.notexmc.freezetag;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import net.notexmc.freezetag.game.FreezeTagManager;
import net.notexmc.freezetag.game.FreezeTagPlayer;
import net.notexmc.freezetag.listeners.FreezeTagListener;
import net.notexmc.freezetag.listeners.GameStartListener;
import net.notexmc.freezetag.listeners.LobbyListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;


public class FreezeTagPlugin extends JavaPlugin {

    private FreezeTagManager gameManager;
    private PlayerManager playerManager;
    private final FileConfiguration worldsConfig;
    private LobbyListener lobbyListener;
    public final File playersFile = new File(getDataFolder(), "players.yml");
    private final Map<UUID, String> registeredPlayers;

    public FreezeTagPlugin() {
        worldsConfig = createWorldsConfig();
        registeredPlayers = new HashMap<>();
    }

    @Override
    public void onEnable() {
        // Load configuration, create arena (optional)
        playerManager = new PlayerManager(this);
        gameManager = new FreezeTagManager(this);

        long start = System.currentTimeMillis();

        getLogger().log(Level.INFO, "  ______ _____  ______ ______ ____________ _______       _____ ");
        getLogger().log(Level.INFO, " |  ____|  __ \\|  ____|  ____|___  /  ____|__   __/\\   / ____|");
        getLogger().log(Level.INFO, " | |__  | |__) | |__  | |__     / /| |__     | |  /  \\ | |  __ ");
        getLogger().log(Level.INFO, " |  __| |  _  /|  __| |  __|   / / |  __|    | | / /\\ \\| | |_ |");
        getLogger().log(Level.INFO, " | |    | | \\ \\| |____| |____ / /__| |____   | |/ ____ \\| |__| |");
        getLogger().log(Level.INFO, " |_|    |_|  \\_\\______|______/_____|______|  |_/_/    \\_\\_____|");
        getLogger().log(Level.INFO, "                                                                ");
        getLogger().log(Level.INFO, "                                                                ");
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "Version: 0.1.8-R0.2-SNAPSHOT");
        getLogger().log(Level.INFO, "Author: Twily");
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "Loading game...");
        getLogger().log(Level.INFO, "Loading player manager...");
        getLogger().log(Level.INFO, "Loading game manager...");
        getLogger().log(Level.INFO, "[FreezeTag]: " + "Loaded in " + (System.currentTimeMillis() - start) + "ms");

        // Check if using Spigot
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException ex) {
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getLogger().severe("FreezeTag requires Spigot to run; you can download");
            getLogger().severe("Spigot here: https://www.spigotmc.org/wiki/spigot-installation/.");
            getLogger().severe("The plugin will now disable.");
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Register events
        getServer().getPluginManager().registerEvents(new FreezeTagListener(this), this);
        getServer().getPluginManager().registerEvents(new GameStartListener(this), this);
        lobbyListener = new LobbyListener(this); // Pass "this" reference
        getServer().getPluginManager().registerEvents(lobbyListener, this);
        FreezeTagMapCommand mapCommand = new FreezeTagMapCommand(this);
        getCommand("freezetag").setExecutor(mapCommand);
        SetLobbyCommand setLobbyCommand = new SetLobbyCommand(this);
        getCommand("setlobby").setExecutor(setLobbyCommand);
        LobbyCommand lobbyCommand = new LobbyCommand(this);
        getCommand("lobby").setExecutor(lobbyCommand);
    }

    public LobbyListener getLobbyListener() {
        return lobbyListener;
    }
    
    public void onDisable() {
        getLogger().log(Level.INFO, "[FreezeTag]: FreezeTag has been disabled.");
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
  
    public File getPlayersFile() {
        return playersFile;
    }

    public FreezeTagManager getGameManager() {
        return gameManager;
    }

    public FileConfiguration getWorldsConfig() {
        return worldsConfig;
    }

    private void loadRegisteredPlayers() {
      if (!playersFile.exists()) {
          try {
              playersFile.createNewFile();
          } catch (IOException e) {
              getLogger().severe("Error creating players.yml file!");
              e.printStackTrace();
          }
      }

      FileConfiguration config = YamlConfiguration.loadConfiguration(playersFile);
      for (String playerName : config.getKeys(false)) {
          registeredPlayers.put(UUID.fromString(playerName), config.getString(playerName));
      }
    }

    private void saveRegisteredPlayers() {
      FileConfiguration config = YamlConfiguration.loadConfiguration(playersFile);
      for (Map.Entry<UUID, String> entry : registeredPlayers.entrySet()) {
          config.set(entry.getKey().toString(), entry.getValue());
      }

      try {
          config.save(playersFile);
      } catch (IOException e) {
          getLogger().severe("Error saving players.yml file!");
          e.printStackTrace();
      }
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();
      if (!registeredPlayers.containsKey(uuid)) {
          registeredPlayers.put(uuid, player.getName() + ": []"); // Register player
          event.getPlayer().sendMessage("§aWelcome to FreezeTag!");
          saveRegisteredPlayers(); // Save registration
      } else {
          event.getPlayer().sendMessage("§aWelcome back to FreezeTag!");
      }
    }


    
    // Helper methods for world configuration

    private FileConfiguration createWorldsConfig() {
        File configFile = new File(getDataFolder(), "worlds.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("worlds.yml", false);
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void saveWorldsConfig() {
        try {
            worldsConfig.save(new File(getDataFolder(), "worlds.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class FreezeTagMapCommand implements CommandExecutor {

        private final FreezeTagPlugin plugin;
        private final FileConfiguration worldsConfig;

        public FreezeTagMapCommand(FreezeTagPlugin plugin) {
            this.plugin = plugin;
            this.worldsConfig = ((FreezeTagPlugin) this.plugin).getWorldsConfig(); // Access worldsConfig from FreezeTagPlugin
        }

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 0) {
                sender.sendMessage("[FreezeTag]: Running 0.1.8-R0.2-SNAPSHOT");
            } else if (args[0].equalsIgnoreCase("map") && (sender.hasPermission("freezetag.map") || sender instanceof ConsoleCommandSender)) {
                if (args.length == 1) {
                    // Display available subcommands
                    sender.sendMessage("[FreezeTag]: Available Subcommands for /freezetag map: register, delete");
                } else if (args.length == 3 && args[1].equalsIgnoreCase("join") && args[2].equalsIgnoreCase("random")) {
                    handleRandomMapJoin(sender);
                } else if (args.length == 3 && args[1].equalsIgnoreCase("join")) {
                    handleSpecifiedMapJoin(sender, args[2]);
                } else if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
                  if (sender.hasPermission("freezetag.map.list")) {
                    handleMapList(sender);
                  }
                } else if (args.length == 2 && args[1].equalsIgnoreCase("register")) {
                  if (sender.hasPermission("freezetag.map.register")) {
                    handleMapRegister(sender);
                  }    
                } else if (args.length == 2 && args[1].equalsIgnoreCase("delete")) {
                  if (sender.hasPermission("freezetag.map.delete")) {
                    handleMapDelete(sender);
                  }
                } else if (args.length == 3 && args[1].equalsIgnoreCase("register")) {
                  if (sender.hasPermission("freezetag.map.register")) {
                    handleMapRegister(sender, args[2]);
                  }
                } else if (args.length == 3 && args[1].equalsIgnoreCase("delete")) {
                  if (sender.hasPermission("freezetag.map.delete")) {
                    handleMapDelete(sender, args[2]);
                  }
                } else {
                    sender.sendMessage("[FreezeTag]: Invalid Usage. Use /freezetag map [register/delete] [worldname]");
                }
            } else {
                sender.sendMessage("[FreezeTag]: Invalid");
            }
            return true;
        }


        private void teleportToMap(Player player, String worldName) {
            player.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
            player.sendMessage("[FreezeTag]: You've joined the game on the map " + worldName + "!");
        }

        private void handleMapList(CommandSender sender) {
            if (plugin.getWorldsConfig().contains("worlds")) {
                List<String> registeredWorlds = plugin.getWorldsConfig().getStringList("worlds");
                sender.sendMessage("Available maps:");

                for (String world : registeredWorlds) {
                    sender.sendMessage("- " + world);
                }
            } else {
                sender.sendMessage("No maps available.");
            }
        }
      
        private void handleMapRegister(CommandSender sender) {
            if (sender instanceof Player) {
                registerCurrentWorld(sender);
            } else if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("[FreezeTag]: This command must be executed by a player in-game with the world name.");
            }
        }

        private void handleMapRegister(CommandSender sender, String worldName) {
            if (sender instanceof Player) {
                registerSpecifiedWorld(sender, worldName);
            } else if (sender instanceof ConsoleCommandSender) {
                registerSpecifiedWorld(sender, worldName);
            }
        }

        private void handleMapDelete(CommandSender sender) {
            if (sender instanceof Player) {
                sender.sendMessage("[FreezeTag]: To delete a world, specify the world name. Use /freezetag map delete <worldname>");
            } else if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("[FreezeTag]: This command must be executed by a player in-game with the world name.");
            }
        }

        private void handleMapDelete(CommandSender sender, String worldName) {
            if (sender instanceof Player) {
                deleteWorld(sender, worldName);
            } else if (sender instanceof ConsoleCommandSender) {
                deleteWorld(sender, worldName);
            }
        }

        private void registerCurrentWorld(CommandSender sender) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String worldName = player.getWorld().getName();
                registerWorld(worldName, sender);
            } else {
                sender.sendMessage("This command can only be executed by players in-game.");
            }
        }

        private void registerSpecifiedWorld(CommandSender sender, String worldName) {
            if (getServer().getWorlds().stream().anyMatch(world -> world.getName().equalsIgnoreCase(worldName))) {
                registerWorld(worldName, sender);
            } else {
                sender.sendMessage("World " + worldName + " does not exist.");
            }
        }

        private void registerWorld(String worldName, CommandSender sender) {
            List<String> registeredWorlds = worldsConfig.getStringList("worlds");

            if (isWorldRegistered(worldName)) {
                sender.sendMessage("World " + worldName + " is already registered.");
                return;
            }

            registeredWorlds.add(worldName);
            worldsConfig.set("worlds", registeredWorlds);
            saveWorldsConfig();

            sender.sendMessage("World " + worldName + " registered successfully.");
        }

        private void handleRandomMapJoin(CommandSender sender) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                List<String> registeredWorlds = plugin.getWorldsConfig().getStringList("worlds");

                // Sort worlds by player count in descending order
                registeredWorlds.sort((world1, world2) -> {
                    int playerCount1 = Bukkit.getWorld(world1).getPlayers().size();
                    int playerCount2 = Bukkit.getWorld(world2).getPlayers().size();
                    return Integer.compare(playerCount2, playerCount1);
                });

                for (String world : registeredWorlds) {
                    int playerCount = Bukkit.getWorld(world).getPlayers().size();
                    if (playerCount > 0) {
                        // Teleport the player to the world with the highest player count
                        teleportToMap(player, world);
                        return;
                    }
                }

                sender.sendMessage("[FreezeTag]: No maps available to join.");
            } else {
                sender.sendMessage("[FreezeTag]: This command must be executed by a player.");
            }
        }

        private void handleSpecifiedMapJoin(CommandSender sender, String worldName) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (isWorldRegistered(worldName)) {
                    int playerCount = Bukkit.getWorld(worldName).getPlayers().size();

                    if (playerCount > 0) {
                        // Teleport the player to the specified world
                        teleportToMap(player, worldName);
                    } else {
                        sender.sendMessage("[FreezeTag]: The specified world has no players.");
                    }
                } else {
                    sender.sendMessage("[FreezeTag]: World " + worldName + " is not registered.");
                }
            } else {
                sender.sendMessage("[FreezeTag]: This command must be executed by a player.");
            }
        }

        private boolean isWorldRegistered(String worldName) {
            List<String> registeredWorlds = plugin.getWorldsConfig().getStringList("worlds");
            return registeredWorlds.contains(worldName);
        }

        private void deleteWorld(CommandSender sender, String worldName) {
            List<String> worldList = worldsConfig.getStringList("worlds");

            if (!isWorldRegistered(worldName)) {
                sender.sendMessage("[FreezeTag]: " + "World " + worldName + " is not registered.");
                return;
            }

            // Remove the specified world from the list
            worldList.removeIf(w -> w.equalsIgnoreCase(worldName));
            worldsConfig.set("worlds", worldList);
            saveWorldsConfig();

            sender.sendMessage("World " + worldName + " deleted successfully.");
        } 
    }

    public class SetLobbyCommand implements CommandExecutor {

        private final FreezeTagPlugin plugin;

        public SetLobbyCommand(FreezeTagPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by a player.");
                return true;
            }

            Player player = (Player) sender;

            // Check for the correct number of arguments
            if (args.length != 1 || !args[0].equalsIgnoreCase("freezetag")) {
                player.sendMessage("Usage: /setlobby freezetag");
                return true;
            }

            // Get the player's current location
            Location location = player.getLocation();

            // Serialize location to string
            String locationString = serializeLocation(location);

            // Save the location to the configuration
            plugin.getWorldsConfig().set("lobby-world", "freezetag");
            plugin.getWorldsConfig().set("lobby-location", locationString);
            plugin.saveWorldsConfig();

            player.sendMessage("Lobby location for Freezetag set!");

            return true;
        }

        // Helper method to serialize location to string
        private String serializeLocation(Location location) {
            return location.getX() + "," + location.getY() + "," + location.getZ();
        }
    }

    public class LobbyCommand implements CommandExecutor {

        private final FreezeTagPlugin plugin;

        public LobbyCommand(FreezeTagPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by a player.");
                return true;
            }
            if (sender.hasPermission("freezetag.setlobby")) {
              Player player = (Player) sender;

              // Check if configuration exists
              if (plugin.getWorldsConfig().contains("lobby-world") && plugin.getWorldsConfig().contains("lobby-location")) {

                  // Get world and location information
                  String worldName = plugin.getWorldsConfig().getString("lobby-world");
                  String locationString = plugin.getWorldsConfig().getString("lobby-location");

                  // Reuse the deserializeLocation method from LobbyListener
                  Location location = deserializeLocation(worldName, locationString);

                  // Check if location is valid
                  if (location != null) {
                      player.teleport(location);
                      player.sendMessage("Teleported to Freezetag lobby!");
                  } else {
                      plugin.getLogger().warning("Invalid lobby world or location in configuration!");
                  }
              } else {
                  player.sendMessage("Lobby configuration not set. Use /setlobby freezetag to set the lobby location.");
              }
            }

            return true;
        }

        // Reuse the deserializeLocation method from LobbyListener
        public Location deserializeLocation(String worldName, String locationString) {
            return plugin.getLobbyListener().deserializeLocation(worldName, locationString);
        }
    }


}

