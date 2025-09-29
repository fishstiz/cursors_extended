package io.github.fishstiz.cursors_extended.platform.services;

import com.google.common.hash.Hashing;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.fishstiz.cursors_extended.CursorsExtended.MOD_ID;

public interface PackHashing {
    Stream<PackResources> fetchResources(ResourceManager manager, ResourceLocation directory);

    default Optional<String> aggregateHash(ResourceManager manager, ResourceLocation directory) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        fetchResources(manager, directory).forEach(packResources -> {
            try (packResources) {
                writeBytes(out, directory.getPath(), packResources);
            }
        });

        return out.size() == 0
                ? Optional.empty()
                : Optional.of(Hashing.murmur3_32_fixed().hashBytes(out.toByteArray()).toString());
    }

    private void writeBytes(ByteArrayOutputStream out, String path, PackResources packResources) {
        MutableBoolean listed = new MutableBoolean(false);

        packResources.listResources(PackType.CLIENT_RESOURCES, MOD_ID, path, (resourceLocation, ioSupplier) -> {
            try (InputStream in = ioSupplier.get()) {
                if (listed.isFalse()) {
                    try {
                        out.write(packResources.location().id().getBytes(StandardCharsets.UTF_8));
                        listed.setTrue();
                    } catch (IOException ignore) {
                    }
                }
                if (resourceLocation.getPath().endsWith(CursorMetadata.FILE_TYPE)) {
                    try {
                        in.transferTo(out);
                    } catch (IOException ignore) {
                    }
                }
            } catch (IOException ignore) {
            }
        });
    }
}