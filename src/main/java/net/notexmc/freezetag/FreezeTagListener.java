package net.notexmc.freezetag.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.PlayerDeathEvent; // Corrected import statement
import org.bukkit.event.block.Action;


import net.notexmc.freezetag.FreezeTagPlugin;
import net.notexmc.freezetag.game.FreezeTagPlayer;
import net.notexmc.freezetag.game.Role;

public class FreezeTagListener implements Listener {

    private final FreezeTagPlugin plugin;

    public FreezeTagListener(FreezeTagPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        FreezeTagPlayer player = plugin.getGameManager().getPlayer(event.getPlayer().getUniqueId());
        if (player.isFrozen()) {
            event.setTo(event.getFrom()); // Prevent movement
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
      FreezeTagPlayer player = plugin.getGameManager().getPlayer(event.getPlayer().getUniqueId());
      if (player.getRole() == Role.ICE) {
        // Check if clicking a water player and not frozen
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR) {
          FreezeTagPlayer targetPlayer = plugin.getGameManager().getPlayer(event.getPlayer().getUniqueId());
          player.setTarget(targetPlayer);
          // FreezeTagPlayer targetPlayer = player.getTarget();
          if (targetPlayer != null && player.getTarget() == targetPlayer && !targetPlayer.isFrozen()) {
            targetPlayer.freeze(5000); // Freeze target for 5 seconds (adjust duration)
          }
        }
      }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player) {
            FreezeTagPlayer player = plugin.getGameManager().getPlayer(event.getEntity().getUniqueId());
            if (player.isFrozen()) {
                // Handle frozen player death (optional: specific death message)
                event.setDeathMessage("§cYou were eliminated while frozen!");
            } else {
                plugin.getPlayerManager().removePlayer(player); // Player eliminated
            }
        }
    }

    // Additional event handlers for specific interactions, power-ups, etc.
}

