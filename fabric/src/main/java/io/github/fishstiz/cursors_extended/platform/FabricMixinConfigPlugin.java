package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class FabricMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "io.github.fishstiz.cursors_extended.mixin";
    private static final String MOD_MENU_MIXIN_PACKAGE = MIXIN_PACKAGE + ".compat.modmenu";

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
        return !mixinClassName.startsWith(MOD_MENU_MIXIN_PACKAGE) || FabricLoader.getInstance().isModLoaded("modmenu");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // Do nothing
    }

    @Override
    public List<String> getMixins() {
//        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
//            return null;
//        }
//
//        if (!CursorsExtended.CONFIG.isWorkaroundsEnabled()) {
//            CursorsExtended.LOGGER.info("[cursors_extended] Compatibility workarounds disabled by config.");
//            return null;
//        }
//
//        if (FabricLauncherBase.getLauncher().isClassLoaded("org.lwjgl.glfw.GLFW")) {
//            CursorsExtended.LOGGER.warn("[cursors_extended] GLFW has been loaded early, unable to apply compatibility workarounds.");
//            return null;
//        }
//
//        return List.of("compat.glfw.GLFWMixin", "compat.glfw.internal.CursorTypeMixin", "compat.glfw.internal.NativeImageUtilMixin");
        return null;
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
