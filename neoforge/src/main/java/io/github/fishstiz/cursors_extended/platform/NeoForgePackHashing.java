package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.platform.services.PackHashing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.stream.Stream;

class NeoForgePackHashing implements PackHashing {
    static final PackHashing INSTANCE = new NeoForgePackHashing();

    private NeoForgePackHashing() {
    }

    @Override
    public Stream<PackResources> fetchResources(ResourceManager manager, ResourceLocation directory) {
        return manager
                .listPacks()
                .flatMap(packResources -> {
                    if (packResources.getNamespaces(PackType.CLIENT_RESOURCES).contains(directory.getNamespace())) {
                        return Stream.of(packResources);
                    } else {
                        packResources.close();
                        return Stream.empty();
                    }
                });
    }
}
