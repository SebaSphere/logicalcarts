package dev.sebastianb.logicalcarts.cart;

import dev.sebastianb.logicalcarts.ExampleMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages active minecarts in the world.
 * This class keeps track of all minecarts that are currently active using wrapper objects that store UUIDs.
 */
public class ActiveMinecartManager {

    private static final ActiveMinecartManager INSTANCE = new ActiveMinecartManager();
    private final Map<UUID, MinecartWrapper> activeMinecarts = new HashMap<>();
    private final List<UUID> removedCarts = new ArrayList<>();
    private int currentTick = 0;
    private World world; // Reference to the world for entity lookups

    // The number of ticks after which a minecart is considered inactive if not updated
    private static final int INACTIVE_THRESHOLD = 5;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ActiveMinecartManager() {
    }

    /**
     * Gets the singleton instance of the ActiveMinecartManager.
     * 
     * @return The singleton instance
     */
    public static ActiveMinecartManager getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the world reference for entity lookups.
     * 
     * @param world The world
     */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Increments the current tick counter.
     * This should be called once per world tick.
     */
    public void incrementTick() {
        currentTick++;
    }

    /**
     * Gets the current tick.
     * 
     * @return The current tick
     */
    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * Adds a minecart to the active minecarts map and updates its last update tick.
     * 
     * @param minecart The minecart to add
     */
    public void addMinecart(EntityMinecart minecart) {
        if (minecart == null) {
            return;
        }

        UUID uuid = minecart.getUniqueID();
        MinecartWrapper wrapper = activeMinecarts.get(uuid);

        if (wrapper == null) {
            // Create a new wrapper if one doesn't exist
            wrapper = new MinecartWrapper(minecart, currentTick);
            activeMinecarts.put(uuid, wrapper);
        } else {
            // Update the existing wrapper
            wrapper.updateTick(currentTick);
            wrapper.updatePosition(minecart.posX, minecart.posY, minecart.posZ);
        }
    }

    /**
     * Removes a minecart from the active minecarts map.
     * 
     * @param minecart The minecart to remove
     */
    public void removeMinecart(EntityMinecart minecart) {
        if (minecart == null) {
            return;
        }

        activeMinecarts.remove(minecart.getUniqueID());
    }

    /**
     * Gets a list of all active minecarts.
     * 
     * @return A list of active minecarts
     */
    public List<EntityMinecart> getActiveMinecarts() {
        List<EntityMinecart> minecarts = new ArrayList<>();

        if (world == null) {
            return minecarts;
        }

        for (MinecartWrapper wrapper : activeMinecarts.values()) {
            EntityMinecart minecart = wrapper.findMinecart(world);
            if (minecart != null) {
                minecarts.add(minecart);
            }
        }

        return minecarts;
    }

    /**
     * Gets the number of active minecarts.
     * 
     * @return The number of active minecarts
     */
    public int getActiveMinecartCount() {
        return activeMinecarts.size();
    }

    /**
     * Cleans up invalid minecarts from the active minecarts map.
     * This should be called periodically to remove minecarts that are no longer valid.
     */
    public void cleanupInvalidMinecarts() {
        if (world == null) {
            return;
        }

        Iterator<Map.Entry<UUID, MinecartWrapper>> iterator = activeMinecarts.entrySet().iterator();
        int removedCount = 0;
        List<MinecartWrapper> removedMinecarts = new ArrayList<>();

        while (iterator.hasNext()) {
            Map.Entry<UUID, MinecartWrapper> entry = iterator.next();
            MinecartWrapper wrapper = entry.getValue();
            EntityMinecart minecart = wrapper.findMinecart(world);

            if (minecart == null || minecart.isDead) {
                removedMinecarts.add(wrapper);
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            // Log summary at info level
            ExampleMod.LOGGER.info("Removed {} invalid minecarts from the ActiveMinecartManager", removedCount);

            // Log details about each removed minecart
            for (int i = 0; i < removedMinecarts.size(); i++) {
                MinecartWrapper wrapper = removedMinecarts.get(i);
                ExampleMod.LOGGER.info("Removed invalid minecart {}: {} at ({}, {}, {}) - Last updated {} ticks ago",
                    i + 1, 
                    wrapper.getMinecartType(),
                    String.format("%.2f", wrapper.getLastX()), 
                    String.format("%.2f", wrapper.getLastY()), 
                    String.format("%.2f", wrapper.getLastZ()),
                    currentTick - wrapper.getLastUpdateTick());
            }
        }
    }

    /**
     * Removes minecarts that haven't been updated for a specified number of ticks.
     * 
     * @return The number of minecarts removed
     */
    public int removeInactiveMinecarts() {
        Iterator<Map.Entry<UUID, MinecartWrapper>> iterator = activeMinecarts.entrySet().iterator();
        int removedCount = 0;
        List<MinecartWrapper> removedMinecarts = new ArrayList<>();

        while (iterator.hasNext()) {
            Map.Entry<UUID, MinecartWrapper> entry = iterator.next();
            MinecartWrapper wrapper = entry.getValue();
            int lastUpdateTick = wrapper.getLastUpdateTick();
            int ticksSinceLastUpdate = currentTick - lastUpdateTick;

            if (ticksSinceLastUpdate > INACTIVE_THRESHOLD) {
                removedMinecarts.add(wrapper);
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            // Log summary at info level
            ExampleMod.LOGGER.info("Removed {} inactive minecarts that haven't been updated for {} ticks", 
                removedCount, INACTIVE_THRESHOLD);

            // Log details about each removed minecart
            for (int i = 0; i < removedMinecarts.size(); i++) {
                MinecartWrapper wrapper = removedMinecarts.get(i);

                this.removeRealCart(wrapper);

                ExampleMod.LOGGER.info("  Removed minecart {}: {} at ({}, {}, {}) - Last updated {} ticks ago", 
                    i + 1, 
                    wrapper.getMinecartType(),
                    String.format("%.2f", wrapper.getLastX()), 
                    String.format("%.2f", wrapper.getLastY()), 
                    String.format("%.2f", wrapper.getLastZ()),
                    currentTick - wrapper.getLastUpdateTick());
            }
        }

        return removedCount;
    }

    private void removeRealCart(MinecartWrapper minecartWrapper) {
        if (world == null) return;

        UUID minecartUUID = minecartWrapper.getMinecartUUID();
        removedCarts.add(minecartUUID);
        ExampleMod.LOGGER.info("Added minecart with UUID {} to removedCarts list", minecartUUID);
    }

    /**
     * Checks if a minecart UUID is in the removedCarts list and removes it if found.
     * 
     * @param uuid The UUID of the minecart to check
     * @return true if the minecart was in the removedCarts list and was removed, false otherwise
     */
    public boolean checkAndRemoveFromRemovedCarts(UUID uuid) {
        boolean wasRemoved = removedCarts.remove(uuid);
        if (wasRemoved) {
            ExampleMod.LOGGER.info("Removed minecart with UUID {} from removedCarts list", uuid);
        }
        return wasRemoved;
    }

    /**
     * Logs the current state of the ActiveMinecartManager.
     * This is useful for debugging.
     */
    public void logState() {
        ExampleMod.LOGGER.info("ActiveMinecartManager: {} active minecarts at tick {}", 
            activeMinecarts.size(), currentTick);

        // Log details about each minecart if in debug mode

        int i = 0;
        for (MinecartWrapper wrapper : activeMinecarts.values()) {
            ExampleMod.LOGGER.debug("  Minecart {}: {} at ({}, {}, {}) - Last updated {} ticks ago",
                    i++,
                    wrapper.getMinecartType(),
                    wrapper.getLastX(),
                    wrapper.getLastY(),
                    wrapper.getLastZ(),
                    currentTick - wrapper.getLastUpdateTick());
        }

    }
}
