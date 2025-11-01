package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.compat.glfw.GLFWInternal;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class FabricMixinConfigPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("cursors_extended | Mixin");
    private static final String MIXIN_PACKAGE = "io.github.fishstiz.cursors_extended.mixin";
    private static final String MOD_MENU_MIXIN_PACKAGE = MIXIN_PACKAGE + ".compat.modmenu";
    private static final String GLFW_INTERNAL_MIXIN_PACKAGE = MIXIN_PACKAGE + ".compat.glfw.internal";

    @Override
    public void onLoad(String mixinPackage) {
        // Do nothing
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(MOD_MENU_MIXIN_PACKAGE) && !FabricLoader.getInstance().isModLoaded("modmenu")) {
            return false;
        }
        if (mixinClassName.startsWith(GLFW_INTERNAL_MIXIN_PACKAGE) && !GLFWInternal.isEnabled()) {
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // Do nothing
    }

    @Override
    public List<String> getMixins() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return null;
        }

        if (FabricLauncherBase.getLauncher().isClassLoaded("org.lwjgl.glfw.GLFW")) {
            GLFWInternal.setEnabled(false);
            LOGGER.warn("[cursors_extended] GLFW has been loaded early, unable to apply compatibility workarounds.");
            return null;
        }

        return List.of("compat.glfw.GLFWMixin");
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Do nothing
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Do nothing
    }
}
