package game.ui.common;

import java.awt.*;
//======================================================================================================================
// 반응형 레이아웃 (게임 창 확대 축소하면 맞춰서 늘어나고 줄어드는거, ui 하나하나 만지다가 이건 진짜 시간이 너무 걸려서 그냥 ai써서 해결)
//======================================================================================================================
public final class ResponsiveGridLayout implements LayoutManager2 {
    private final int maxColumns;
    private final int minCellWidth;
    private final int minCellHeight;
    private final int horizontalGap;
    private final int verticalGap;

    public ResponsiveGridLayout(int maxColumns, int minCellWidth, int minCellHeight,
                                int horizontalGap, int verticalGap) {
        if (maxColumns < 1 || minCellWidth < 1 || minCellHeight < 1) {
            throw new IllegalArgumentException("그리드 크기는 양수여야 합니다.");
        }
        this.maxColumns = maxColumns;
        this.minCellWidth = minCellWidth;
        this.minCellHeight = minCellHeight;
        this.horizontalGap = Math.max(0, horizontalGap);
        this.verticalGap = Math.max(0, verticalGap);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = Math.max(0, parent.getWidth() - insets.left - insets.right);
            int height = Math.max(0, parent.getHeight() - insets.top - insets.bottom);
            int count = visibleComponentCount(parent);
            if (count == 0) return;

            int columns = Math.min(count, Math.min(maxColumns,
                    Math.max(1, (width + horizontalGap) / (minCellWidth + horizontalGap))));
            int rows = (count + columns - 1) / columns;
            int cellWidth = Math.max(1, (width - horizontalGap * (columns - 1)) / columns);
            int cellHeight = Math.max(1, (height - verticalGap * (rows - 1)) / rows);

            int visibleIndex = 0;
            for (Component component : parent.getComponents()) {
                if (!component.isVisible()) continue;
                int row = visibleIndex / columns;
                int column = visibleIndex % columns;
                int x = insets.left + column * (cellWidth + horizontalGap);
                int y = insets.top + row * (cellHeight + verticalGap);
                component.setBounds(x, y, cellWidth, cellHeight);
                visibleIndex++;
            }
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int count = visibleComponentCount(parent);
            int columns = Math.min(maxColumns, Math.max(1, count));
            int rows = count == 0 ? 0 : (count + columns - 1) / columns;
            return new Dimension(insets.left + insets.right + columns * minCellWidth
                    + horizontalGap * Math.max(0, columns - 1),
                    insets.top + insets.bottom + rows * minCellHeight
                            + verticalGap * Math.max(0, rows - 1));
        }
    }

    @Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(0, 0); }
    @Override public Dimension maximumLayoutSize(Container target) { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }
    @Override public float getLayoutAlignmentX(Container target) { return 0.5f; }
    @Override public float getLayoutAlignmentY(Container target) { return 0.5f; }
    @Override public void invalidateLayout(Container target) { }
    @Override public void addLayoutComponent(Component comp, Object constraints) { }
    @Override public void addLayoutComponent(String name, Component comp) { }
    @Override public void removeLayoutComponent(Component comp) { }

    private static int visibleComponentCount(Container parent) {
        int count = 0;
        for (Component component : parent.getComponents()) if (component.isVisible()) count++;
        return count;
    }
}
