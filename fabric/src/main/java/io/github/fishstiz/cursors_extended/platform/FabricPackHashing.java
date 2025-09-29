package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.platform.services.PackHashing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.stream.Stream;

class FabricPackHashing implements PackHashing {
    static final PackHashing INSTANCE = new FabricPackHashing();

    private FabricPackHashing() {
    }

    public Stream<PackResources> fetchResources(ResourceManager manager, ResourceLocation directory) {
        return manager.getResourceStack(directory).stream().map(Resource::source);
    }
}
