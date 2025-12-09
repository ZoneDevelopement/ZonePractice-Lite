package dev.nandi0813.practice.Util.EntityHider;

import java.util.Map;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class EntityHider implements Listener {
    protected Table<Integer, Integer, Boolean> observerEntityMap = HashBasedTable.create();

    /**
     * The current entity visibility policy.
     *
     * @author Kristian
     */
    public enum Policy {
        /**
         * All entities are invisible by default. Only entities specifically made
         * visible may be seen.
         */
        WHITELIST,

        /**
         * All entities are visible by default. An entity can only be hidden explicitly.
         */
        BLACKLIST,
    }

    // Listeners
    private final Listener bukkitListener;
    private final PacketListenerCommon peListener;

    // Current policy
    protected final Policy policy;

    /**
     * Construct a new entity hider.
     *
     * @param plugin - the plugin that controls this entity hider.
     * @param policy - the default visibility policy.
     */
    public EntityHider(Plugin plugin, Policy policy) {
        Preconditions.checkNotNull(plugin, "plugin cannot be NULL.");

        // Save policy
        this.policy = policy;

        // Register events and packet listener
        plugin.getServer().getPluginManager().registerEvents(bukkitListener = constructBukkit(), plugin);
        peListener = PacketEvents.getAPI().getEventManager().registerListener(constructProtocol, PacketListenerPriority.NORMAL);
    }

    /**
     * Set the visibility status of a given entity for a particular observer.
     *
     * @param observer - the observer player.
     * @param entityID - ID of the entity that will be hidden or made visible.
     * @param visible  - TRUE if the entity should be made visible, FALSE if not.
     * @return TRUE if the entity was visible before this method call, FALSE
     * otherwise.
     */
    protected boolean setVisibility(Player observer, int entityID, boolean visible) {
        switch (policy) {
            case BLACKLIST:
                // Non-membership means they are visible
                return !setMembership(observer, entityID, !visible);
            case WHITELIST:
                return setMembership(observer, entityID, visible);
            default:
                throw new IllegalArgumentException("Unknown policy: " + policy);
        }
    }

    /**
     * Add or remove the given entity and observer entry from the table.
     *
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @param member   - TRUE if they should be present in the table, FALSE
     *                 otherwise.
     * @return TRUE if they already were present, FALSE otherwise.
     */
    // Helper method
    protected boolean setMembership(Player observer, int entityID, boolean member) {
        if (member) {
            return observerEntityMap.put(observer.getEntityId(), entityID, true) != null;
        } else {
            return observerEntityMap.remove(observer.getEntityId(), entityID) != null;
        }
    }

    /**
     * Determine if the given entity and observer is present in the table.
     *
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @return TRUE if they are present, FALSE otherwise.
     */
    protected boolean getMembership(Player observer, int entityID) {
        return observerEntityMap.contains(observer.getEntityId(), entityID);
    }

    /**
     * Determine if a given entity is visible for a particular observer.
     *
     * @param observer - the observer player.
     * @param entityID - ID of the entity that we are testing for visibility.
     * @return TRUE if the entity is visible, FALSE otherwise.
     */
    protected boolean isVisible(Player observer, int entityID) {
        // If we are using a whitelist, presence means visibility - if not, the opposite
        // is the case
        boolean presence = getMembership(observer, entityID);

        return (policy == Policy.WHITELIST) == presence;
    }

    /**
     * Remove the given entity from the underlying map.
     *
     * @param entity    - the entity to remove.
     * @param destroyed - TRUE if the entity was killed, FALSE if it is merely
     *                  unloading.
     */
    protected void removeEntity(Entity entity, boolean destroyed) {
        int entityID = entity.getEntityId();

        for (Map<Integer, Boolean> maps : observerEntityMap.rowMap().values()) {
            maps.remove(entityID);
        }
    }

    /**
     * Invoked when a player logs out.
     *
     * @param player - the player that jused logged out.
     */
    protected void removePlayer(Player player) {
        // Cleanup
        observerEntityMap.rowMap().remove(player.getEntityId());
    }

    /**
     * Construct the Bukkit event listener.
     *
     * @return Our listener.
     */
    private Listener constructBukkit() {
        return new Listener() {
            @EventHandler
            public void onEntityDeath(EntityDeathEvent e) {
                removeEntity(e.getEntity(), true);
            }

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent e) {
                for (Entity entity : e.getChunk().getEntities()) {
                    removeEntity(entity, false);
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                removePlayer(e.getPlayer());
            }
        };
    }

    /**
     * Construct the packet listener that will be used to intercept every
     * entity-related packet.
     *
     * @param plugin - the parent plugin.
     * @return The packet listener.
     */
    private PacketListener constructProtocol = new PacketListener() {
        @Override
        public void onPacketSend(PacketSendEvent event) {
            int entityID = switch (event.getPacketType()) {
                case PacketType.Play.Server.ENTITY_EQUIPMENT ->
                        new WrapperPlayServerEntityEquipment(event).getEntityId();
                case PacketType.Play.Server.ENTITY_HEAD_LOOK ->
                        new WrapperPlayServerEntityHeadLook(event).getEntityId();
                case PacketType.Play.Server.SPAWN_ENTITY ->
                        new WrapperPlayServerSpawnEntity(event).getEntityId();
                case PacketType.Play.Server.COLLECT_ITEM ->
                        new WrapperPlayServerCollectItem(event).getCollectedEntityId();
                case PacketType.Play.Server.SPAWN_PAINTING ->
                        new WrapperPlayServerSpawnPainting(event).getEntityId();
                case PacketType.Play.Server.SPAWN_EXPERIENCE_ORB ->
                        new WrapperPlayServerSpawnExperienceOrb(event).getEntityId();
                case PacketType.Play.Server.ENTITY_VELOCITY ->
                        new WrapperPlayServerEntityVelocity(event).getEntityId();
                case PacketType.Play.Server.ENTITY_TELEPORT ->
                        new WrapperPlayServerEntityTeleport(event).getEntityId();
                case PacketType.Play.Server.ENTITY_ROTATION ->
                        new WrapperPlayServerEntityRotation(event).getEntityId();
                case PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION ->
                        new WrapperPlayServerEntityRelativeMoveAndRotation(event).getEntityId();
                case PacketType.Play.Server.ENTITY_RELATIVE_MOVE ->
                        new WrapperPlayServerEntityRelativeMove(event).getEntityId();
                case PacketType.Play.Server.ENTITY_STATUS ->
                        new WrapperPlayServerEntityStatus(event).getEntityId();
                case PacketType.Play.Server.ATTACH_ENTITY ->
                        new WrapperPlayServerAttachEntity(event).getAttachedId();
                case PacketType.Play.Server.ENTITY_SOUND_EFFECT ->
                        new WrapperPlayServerEntitySoundEffect(event).getEntityId();
                case PacketType.Play.Server.ENTITY_METADATA ->
                        new WrapperPlayServerEntityMetadata(event).getEntityId();
                case PacketType.Play.Server.ENTITY_EFFECT ->
                        new WrapperPlayServerEntityEffect(event).getEntityId();
                case PacketType.Play.Server.REMOVE_ENTITY_EFFECT ->
                        new WrapperPlayServerRemoveEntityEffect(event).getEntityId();
                case PacketType.Play.Server.BLOCK_BREAK_ANIMATION ->
                        new WrapperPlayServerBlockBreakAnimation(event).getEntityId();
                case PacketType.Play.Server.ENTITY_ANIMATION ->
                        new WrapperPlayServerEntityAnimation(event).getEntityId();
                default -> -1;
            };

            if (entityID != -1 && !isVisible(event.getPlayer(), entityID)) {
                event.setCancelled(true);
            }
        }
    };

    /**
     * Toggle the visibility status of an entity for a player.
     * <p>
     * If the entity is visible, it will be hidden. If it is hidden, it will become
     * visible.
     *
     * @param observer - the player observer.
     * @param entity   - the entity to toggle.
     * @return TRUE if the entity was visible before, FALSE otherwise.
     */
    public final boolean toggleEntity(Player observer, Entity entity) {
        if (isVisible(observer, entity.getEntityId())) {
            return hideEntity(observer, entity);
        } else {
            return !showEntity(observer, entity);
        }
    }

    /**
     * Allow the observer to see an entity that was previously hidden.
     *
     * @param observer - the observer.
     * @param entity   - the entity to show.
     * @return TRUE if the entity was hidden before, FALSE otherwise.
     */
    public final boolean showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean hiddenBefore = !setVisibility(observer, entity.getEntityId(), true);

        // Resend packets
        if (hiddenBefore) {
            // 1. Send spawn packet
            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                    entity.getEntityId(),
                    entity.getUniqueId(),
                    EntityTypes.getById(PacketEvents.getAPI().getPlayerManager().getUser(observer).getClientVersion(),
                            entity.getType().getTypeId()), // You might need to map this
                    SpigotConversionUtil.fromBukkitLocation(entity.getLocation()),
                    entity.getLocation().getYaw(), // Head yaw
                    0, // Data
                    new Vector3d() // Velocity
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(observer, spawnPacket);
        }

        return hiddenBefore;
    }

    /**
     * Prevent the observer from seeing a given entity.
     *
     * @param observer - the player observer.
     * @param entity   - the entity to hide.
     * @return TRUE if the entity was previously visible, FALSE otherwise.
     */
    public final boolean hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean visibleBefore = setVisibility(observer, entity.getEntityId(), false);

        if (visibleBefore) {
            WrapperPlayServerDestroyEntities destroyEntity = new WrapperPlayServerDestroyEntities(entity.getEntityId());
            PacketEvents.getAPI().getPlayerManager().sendPacket(observer, destroyEntity);
        }

        return visibleBefore;
    }

    /**
     * Determine if the given entity has been hidden from an observer.
     * <p>
     * Note that the entity may very well be occluded or out of range from the
     * perspective of the observer. This method simply checks if an entity has been
     * completely hidden for that observer.
     *
     * @param observer - the observer.
     * @param entity   - the entity that may be hidden.
     * @return TRUE if the player may see the entity, FALSE if the entity has been
     * hidden.
     */
    public final boolean canSee(Player observer, Entity entity) {
        validate(observer, entity);

        return isVisible(observer, entity.getEntityId());
    }

    // For valdiating the input parameters
    private void validate(Player observer, Entity entity) {
        Preconditions.checkNotNull(observer, "observer cannot be NULL.");
        Preconditions.checkNotNull(entity, "entity cannot be NULL.");
    }

    /**
     * Retrieve the current visibility policy.
     *
     * @return The current visibility policy.
     */
    public Policy getPolicy() {
        return policy;
    }

    public void close() {
        HandlerList.unregisterAll(bukkitListener);
        PacketEvents.getAPI().getEventManager().unregisterListener(peListener);
    }
}