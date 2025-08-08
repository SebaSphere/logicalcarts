package dev.sebastianb.logicalcarts.mixin;

import dev.sebastianb.logicalcarts.ExampleMod;
import dev.sebastianb.logicalcarts.cart.ActiveMinecartManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class WorldMixin {

    // Counter for periodic logging
    private static int logCounter = 0;
    // Log every 600 ticks (10 seconds at 20 ticks per second)
    private static final int LOG_INTERVAL = 200;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        World world = (World)(Object)this;

        // Skip client-side processing
        if (world.isRemote) {
            return;
        }

        // Set the world reference in the ActiveMinecartManager
        ActiveMinecartManager.getInstance().setWorld(world);

        logCounter++;
        // Log state periodically
        if (logCounter >= LOG_INTERVAL) {
            ActiveMinecartManager.getInstance().logState();
            logCounter = 0;
        }

        // Increment the tick counter in ActiveMinecartManager
        ActiveMinecartManager.getInstance().incrementTick();

        // Remove minecarts that haven't been updated for 5 ticks
        // The ActiveMinecartManager will log detailed information about removed minecarts
        ActiveMinecartManager.getInstance().removeInactiveMinecarts();
    }

}
