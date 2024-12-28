package net.notexmc.freezetag.game;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.notexmc.freezetag.FreezeTagPlugin;

public class PlayerSpectator {
    private final Player player; // Reference to the player object
    private final FreezeTagPlugin plugin;

    public PlayerSpectator(Player player, FreezeTagPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void startSpectating() {
        // Set spectator mode, hide player, etc. (use Bukkit APIs)
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);

        // Additional logic for spectator setup can be added here
    }

    // Optional: If you want to add more spectator methods, you can do so here

    public void playFreezeSound() {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);
    }
}
