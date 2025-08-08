package dev.sebastianb.logicalcarts.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("EarlyMixinPlugin")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("dev.sebastianb.logicalcarts.core")
@IFMLLoadingPlugin.SortingIndex(1001)
public class LogicalCartsMixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public LogicalCartsMixinLoader() {
        System.out.println("LogicalCartsMixinLoader loaded!");
    }

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.logicalcarts.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(Context context) {
        System.out.println("ASAWEEF");
        return true;
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
