package io.github.fishstiz.cursors_extended.gui.renderstate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.fishstiz.cursors_extended.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

/**
 * Copied from {@link ColoredRectangleRenderState}, changed bounds to float
 */
public record GuiColoredRectRenderState(
        Matrix3x2f pose,
        float x0,
        float y0,
        float x1,
        float y1,
        int color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds
) implements GuiElementRenderState {
    public static GuiColoredRectRenderState create(
            GuiGraphicsExtractor guiGraphics,
            float x0,
            float y0,
            float x1,
            float y1,
            int color
    ) {
        Matrix3x2f pose = new Matrix3x2f(guiGraphics.pose());
        ScreenRectangle scissorArea = Services.PLATFORM.guiGraphicsHelper().peekScissorStack(guiGraphics);
        ScreenRectangle bounds = new ScreenRectangle((int) x0, (int) y0, (int) (x1 - x0), (int) (y1 - y0)).transformMaxBounds(pose);
        return new GuiColoredRectRenderState(pose, x0, y0, x1, y1, color, scissorArea, scissorArea == null ? bounds : bounds.intersection(bounds));
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.pose, this.x0, this.y0).setColor(this.color);
        vertexConsumer.addVertexWith2DPose(this.pose, this.x0, this.y1).setColor(this.color);
        vertexConsumer.addVertexWith2DPose(this.pose, this.x1, this.y1).setColor(this.color);
        vertexConsumer.addVertexWith2DPose(this.pose, this.x1, this.y0).setColor(this.color);
    }

    @Override
    public @NonNull RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public @NonNull TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }
}
