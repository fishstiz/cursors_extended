package io.github.fishstiz.cursors_extended.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import static io.github.fishstiz.cursors_extended.util.SettingsUtil.clamp;

public abstract class AbstractListWidget<E extends AbstractListWidget<E>.Entry> extends ContainerObjectSelectionList<E> {
    protected final int rowGap;

    protected AbstractListWidget(Minecraft minecraft, int width, int y, int height, int itemHeight) {
        super(minecraft, width, y, height, itemHeight);

        this.rowGap = 0;
    }

    protected AbstractListWidget(Minecraft minecraft, int width, int y, int height, int itemHeight, int rowGap) {
        super(minecraft, width, y, height, itemHeight + rowGap);

        this.rowGap = rowGap;
    }

    @Override
    public int maxScrollAmount() {
        return Math.max(0, super.maxScrollAmount());
    }

    public void setClampedScrollAmount(double scrollAmount) {
        this.setScrollAmount(clamp(scrollAmount, 0d, this.maxScrollAmount()));
    }

    public void clampScrollAmount() {
        this.setClampedScrollAmount(this.scrollAmount());
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - SCROLLBAR_WIDTH;
    }

    @Override
    public int getRowWidth() {
        return this.scrollable() ? this.getWidth() - SCROLLBAR_WIDTH : this.getWidth();
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    @Override
    public int getRowTop(int index) {
        return this.getY() - (int) this.scrollAmount() + index * this.defaultEntryHeight;
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        this.clampScrollAmount();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.clampScrollAmount();
    }

    @Override
    protected int contentHeight() {
        return this.getItemCount() * this.defaultEntryHeight - this.rowGap;
    }

    protected abstract class Entry extends ContainerObjectSelectionList.Entry<E> {
        private final int index;

        protected Entry(int index) {
            this.index = index;
        }

        protected Entry() {
            this(AbstractListWidget.this.children().size());
        }

        @Override
        public int getX() {
            return AbstractListWidget.this.getRowLeft();
        }

        @Override
        public int getY() {
            return AbstractListWidget.this.getRowTop(this.index);
        }

        @Override
        public int getWidth() {
            return AbstractListWidget.this.getRowWidth();
        }

        @Override
        public int getHeight() {
            return AbstractListWidget.this.defaultEntryHeight - AbstractListWidget.this.rowGap;
        }

        public int getRight() {
            return this.getX() + this.getWidth();
        }

        public int getBottom() {
            return this.getY() + this.getHeight();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= this.getX() &&
                   mouseX <= this.getRight() &&
                   mouseY >= this.getY() &&
                   mouseY <= this.getBottom() + AbstractListWidget.this.rowGap;
        }
    }
}
