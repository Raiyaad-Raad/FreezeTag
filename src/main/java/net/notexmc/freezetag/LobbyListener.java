package net.notexmc.freezetag.listeners;

import net.notexmc.freezetag.FreezeTagPlugin;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.ChatColor;
import java.util.List;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.SkullType;

public class LobbyListener implements Listener {

    private final FreezeTagPlugin plugin;

    public LobbyListener(FreezeTagPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        // Check if player is new and configuration exists
        if (!e.getPlayer().hasPlayedBefore() || e.getPlayer().hasPlayedBefore() && plugin.getWorldsConfig().contains("lobby-world") && plugin.getWorldsConfig().contains("lobby-location")) {

            Player player = e.getPlayer();
          
            // Get world and location information
            String worldName = plugin.getWorldsConfig().getString("lobby-world");
            String locationString = plugin.getWorldsConfig().getString("lobby-location");

            player.getInventory().clear();

            // Convert location string to Location object
            Location location = deserializeLocation(worldName, locationString);

            ItemStack head = createPlayerHead(player);

            // Set item display name and lore (optional)
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Stats"); // Set display name to "Stats" in green
            // You can add lore (description) here:
            List<String> lore = Arrays.asList("Click to view stats");
            meta.setLore(lore);
            head.setItemMeta(meta);

            // Add the item to the player's inventory (middle slot - index 4)
          
            // Check if both world and location are valid
            if (location != null) {
                e.getPlayer().teleport(location);
                e.getPlayer().getInventory().setItem(4, head);
            } else {
                plugin.getLogger().warning("Invalid lobby world or location in configuration!");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent e) {

        // Check if configuration exists
        if (plugin.getWorldsConfig().contains("lobby-world") && plugin.getWorldsConfig().contains("lobby-location")) {

            // Get world and location information
            String worldName = plugin.getWorldsConfig().getString("lobby-world");
            String locationString = plugin.getWorldsConfig().getString("lobby-location");

            // Convert location string to Location object
            Location location = deserializeLocation(worldName, locationString);

            // Check if location is valid
            if (location != null) {
                e.setRespawnLocation(location);
            } else {
                plugin.getLogger().warning("Invalid lobby world or location in configuration!");
            }
        }
    }

      // Helper method to convert location string to Location object
    public Location deserializeLocation(String worldName, String locationString) {
        try {
            // Attempt to parse location string
            String[] parts = locationString.split(",");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
  
            // Create and return Location object
            return new Location(Bukkit.getServer().getWorld(worldName), x, y, z);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid location format: " + locationString);
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("Unexpected error while deserializing location: " + e.getMessage());
            return null;
        }
    }

    public Location getLocation() {
      if (plugin.getWorldsConfig().contains("lobby-world") && plugin.getWorldsConfig().contains("lobby-location")) {
        String worldName = plugin.getWorldsConfig().getString("lobby-world");
        String locationString = plugin.getWorldsConfig().getString("lobby-location");
        return deserializeLocation(worldName, locationString);
      } else {
        return null;
      }
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
  
        // Set the player's name as the owner of the head
        meta.setOwningPlayer(player);
        head.setItemMeta(meta);
  
        return head;
    }
  

}