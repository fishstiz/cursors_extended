package io.github.fishstiz.cursors_extended.gui.renderstate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Copied from {@link ColoredRectangleRenderState}, changed bounds to float
 */
public record GuiColoredRectRenderState(
        Matrix3x2f matrix3x2f,
        float x0,
        float y0,
        float x1,
        float y1,
        int color,
        ScreenRectangle bounds
) implements GuiElementRenderState {
    public GuiColoredRectRenderState(
            Matrix3x2f matrix3x2f,
            float x0,
            float y0,
            float x1,
            float y1,
            int color
    ) {
        this(matrix3x2f, x0, y0, x1, y1, color, getBounds(x0, y0, x1, y1, matrix3x2f));
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.matrix3x2f(), this.x0(), this.y0()).setColor(this.color());
        vertexConsumer.addVertexWith2DPose(this.matrix3x2f(), this.x0(), this.y1()).setColor(this.color());
        vertexConsumer.addVertexWith2DPose(this.matrix3x2f(), this.x1(), this.y1()).setColor(this.color());
        vertexConsumer.addVertexWith2DPose(this.matrix3x2f(), this.x1(), this.y0()).setColor(this.color());
    }

    @Override
    public @NonNull RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public @NonNull TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    private static ScreenRectangle getBounds(float x0, float y0, float x1, float y1, Matrix3x2f pose) {
        return new ScreenRectangle((int) x0, (int) y0, (int) (x1 - x0), (int) (y1 - y0)).transformMaxBounds(pose);
    }
}
