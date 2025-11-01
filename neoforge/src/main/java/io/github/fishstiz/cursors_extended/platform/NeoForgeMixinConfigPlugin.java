package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class NeoForgeMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "io.github.fishstiz.cursors_extended.mixin";
    private static final String OWO_PACKAGE = MIXIN_PACKAGE + ".compat.owo";

    private static boolean isModLoaded(String modId) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(OWO_PACKAGE)) {
            if (isModLoaded("owo") && CursorsExtended.CONFIG.isWorkaroundsEnabled()) {
                CursorsExtended.LOGGER.info("[cursors_extended] Applying compatibility with owo-lib.");
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
