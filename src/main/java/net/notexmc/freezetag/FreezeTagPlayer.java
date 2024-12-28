package net.notexmc.freezetag.game;

import org.bukkit.entity.Player;
import org.bukkit.Effect;
import net.notexmc.freezetag.game.Role;

public class FreezeTagPlayer {

    private final Player player;
    private final Role role;
    private boolean frozen;
    private long frozenTime;
    private FreezeTagPlayer target; 

    public FreezeTagPlayer(Player player, Role role) {
        this.player = player;
        this.role = role;
        this.frozen = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Role getRole() {
        return role;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze(long duration) {
        frozen = true;
        frozenTime = System.currentTimeMillis() + duration;
  
        // Set frozen movement speed (adjust values as needed)
        player.setWalkSpeed(0.0F); // Set frozen movement speed using Bukkit API
  
        // Play freezing animation/sound (optional)
        player.getLocation().getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 1); // Adjust parameters as needed
        player.sendMessage("§cYou have been frozen!");
    }
  
    public void unfreeze() {
        frozen = false;
  
        // Set normal movement speed using Bukkit API
        player.setWalkSpeed(0.2F);
  
        // Play unfreezing animation/sound (optional)
        player.getLocation().getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 1); // Adjust parameters as needed
        player.sendMessage("§aYou are now unfrozen!");
    }

    public FreezeTagPlayer getTarget() {
        return target;
    }

    public void setTarget(FreezeTagPlayer target) {
        this.target = target;
    }
  
    // Additional methods for checking remaining freeze time, implementing power-ups, etc.
}
