package dev.nandi0813.practice.Event;

import dev.nandi0813.practice.Manager.Match.Match;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpectateStartEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player spectator;
    @Getter
    private final Match match;
    private boolean cancelled;

    public SpectateStartEvent(Player spectator, Match match) {
        this.spectator = spectator;
        this.match = match;
        cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
