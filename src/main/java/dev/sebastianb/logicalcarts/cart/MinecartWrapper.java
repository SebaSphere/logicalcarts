package dev.sebastianb.logicalcarts.cart;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * A wrapper class for EntityMinecart that stores the UUID of the entity.
 * This allows us to maintain references to minecarts even if the actual entity objects are removed.
 */
public class MinecartWrapper {
    private final UUID minecartUUID;
    private int lastUpdateTick;
    private final String minecartType;
    private double lastX;
    private double lastY;
    private double lastZ;

    /**
     * Creates a new MinecartWrapper for the given minecart.
     *
     * @param minecart The minecart to wrap
     * @param currentTick The current tick when this wrapper is created
     */
    public MinecartWrapper(EntityMinecart minecart, int currentTick) {
        this.minecartUUID = minecart.getUniqueID();
        this.lastUpdateTick = currentTick;
        this.minecartType = minecart.getClass().getSimpleName();
        this.lastX = minecart.posX;
        this.lastY = minecart.posY;
        this.lastZ = minecart.posZ;
    }

    /**
     * Gets the UUID of the wrapped minecart.
     *
     * @return The UUID of the minecart
     */
    public UUID getMinecartUUID() {
        return minecartUUID;
    }

    /**
     * Gets the last update tick of the minecart.
     *
     * @return The last update tick
     */
    public int getLastUpdateTick() {
        return lastUpdateTick;
    }

    /**
     * Updates the last update tick of the minecart.
     *
     * @param currentTick The current tick
     */
    public void updateTick(int currentTick) {
        this.lastUpdateTick = currentTick;
    }

    /**
     * Updates the position of the minecart.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     */
    public void updatePosition(double x, double y, double z) {
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
    }

    /**
     * Gets the type of the minecart.
     *
     * @return The type of the minecart
     */
    public String getMinecartType() {
        return minecartType;
    }

    /**
     * Gets the last known x coordinate of the minecart.
     *
     * @return The last known x coordinate
     */
    public double getLastX() {
        return lastX;
    }

    /**
     * Gets the last known y coordinate of the minecart.
     *
     * @return The last known y coordinate
     */
    public double getLastY() {
        return lastY;
    }

    /**
     * Gets the last known z coordinate of the minecart.
     *
     * @return The last known z coordinate
     */
    public double getLastZ() {
        return lastZ;
    }

    /**
     * Finds the EntityMinecart instance in the given world that corresponds to this wrapper.
     *
     * @param world The world to search in
     * @return The EntityMinecart instance, or null if not found
     */
    public EntityMinecart findMinecart(World world) {
        if (world == null) {
            return null;
        }
        
        for (Object entity : world.loadedEntityList) {
            if (entity instanceof EntityMinecart && ((EntityMinecart) entity).getUniqueID().equals(minecartUUID)) {
                return (EntityMinecart) entity;
            }
        }
        
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MinecartWrapper that = (MinecartWrapper) obj;
        return minecartUUID.equals(that.minecartUUID);
    }

    @Override
    public int hashCode() {
        return minecartUUID.hashCode();
    }
}