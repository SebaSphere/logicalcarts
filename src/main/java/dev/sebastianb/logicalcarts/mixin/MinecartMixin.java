package dev.sebastianb.logicalcarts.mixin;

import dev.sebastianb.logicalcarts.ExampleMod;
import dev.sebastianb.logicalcarts.cart.ActiveMinecartManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityMinecart.class)
public abstract class MinecartMixin extends Entity {



    // Counter for periodic cleanup
    private static int cleanupCounter = 0;
    // Cleanup every 200 ticks (10 seconds at 20 ticks per second)
    private static final int CLEANUP_INTERVAL = 200;


    // This constructor is required by the compiler but won't be used
    private MinecartMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void init(World worldIn, CallbackInfo ci) {

    }

    @Inject(method = "moveAlongTrack", at = @At("HEAD"))
    private void moveAlongTrack(BlockPos pos, IBlockState state, CallbackInfo ci) {

    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
        if (world.isRemote) {
            return;
        }

        // Set the world reference in the ActiveMinecartManager
        ActiveMinecartManager.getInstance().setWorld(world);

        // Get this minecart
        EntityMinecart minecart = (EntityMinecart)(Object)this;

        // Check if this minecart is in the removedCarts list
        boolean wasInRemovedList = ActiveMinecartManager.getInstance().checkAndRemoveFromRemovedCarts(minecart.getUniqueID());

        // If the minecart was in the removedCarts list, set it as dead and log the removal
        if (wasInRemovedList) {
            minecart.setDead();
            ExampleMod.LOGGER.info("Removed real minecart with UUID {} from world", minecart.getUniqueID());
            return;
        }

        // Add this minecart to the ActiveMinecartManager
        ActiveMinecartManager.getInstance().addMinecart(minecart);

        // Increment the counters
        cleanupCounter++;

        // Perform cleanup periodically
        if (cleanupCounter >= CLEANUP_INTERVAL) {
            ActiveMinecartManager.getInstance().cleanupInvalidMinecarts();
            cleanupCounter = 0;
        }
    }

    // so add works but remove only works clientside, doesn't account for unloaded
//    @Override
//    public void onAddedToWorld() {
//        super.onAddedToWorld();
//        System.out.println("Minecart added!");
//    }
//
//    @Override
//    public void onRemovedFromWorld() {
//        super.onRemovedFromWorld();
//        System.out.println("Minecart removed!");
//    }

}
