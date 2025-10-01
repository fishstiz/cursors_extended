package io.github.fishstiz.cursors_extended.lifecycle;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface ClientStartedListener {
    void onClientStarted(Minecraft minecraft);
}
