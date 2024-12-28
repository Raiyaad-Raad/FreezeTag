package net.notexmc.freezetag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import net.notexmc.freezetag.game.Role;
import net.notexmc.freezetag.game.FreezeTagPlayer;

import org.bukkit.entity.Player;

public class PlayerManager {

    private final FreezeTagPlugin plugin;
    private final Map<UUID, FreezeTagPlayer> players;

    public PlayerManager(FreezeTagPlugin plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
    }

    public void addPlayer(Player player, Role role) {
        FreezeTagPlayer newPlayer = new FreezeTagPlayer(player, role);
        players.put(player.getUniqueId(), newPlayer);
    }

    public FreezeTagPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void removePlayer(FreezeTagPlayer player) {
        players.remove(player.getPlayer().getUniqueId());
    }

    public boolean isPlayerFrozen(Player player) {
        FreezeTagPlayer freezeTagPlayer = players.get(player.getUniqueId());
        return freezeTagPlayer != null && freezeTagPlayer.isFrozen();
    }

    public List<FreezeTagPlayer> getPlayersByRole(Role role) {
        List<FreezeTagPlayer> matchingPlayers = new ArrayList<>();
        for (FreezeTagPlayer player : players.values()) {
            if (player.getRole() == role) {
                matchingPlayers.add(player);
            }
        }
        return matchingPlayers;
    }

    public List<FreezeTagPlayer> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    // Additional methods for managing player data, communication, etc.

}
