package org.jeffreypratt.core;

import playn.core.Surface;
import playn.scene.Layer;
import pythagoras.f.IDimension;

public class BoardView extends Layer {
    private static final float LINE_WIDTH = 2;
    private final Reversi game;

    public final float cellSize;

    public BoardView (Reversi game, IDimension viewSize) {
        this.game = game;
        float maxBoardSize = Math.min(viewSize.width(), viewSize.height()) - 20;
        this.cellSize = (float)Math.floor(maxBoardSize / game.boardSize);
    }

    // we want two extra pixels in width/height to account for grid lines
    @Override public float width () { return cellSize * game.boardSize + LINE_WIDTH; }
    @Override public float height () { return width(); }  // width == height

    @Override protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFF000000); // black with full alpha
        float top = 0, bot = height(), left = 0, right = width();

        // draw vertical grid lines
        for (int yy = 0; yy <= game.boardSize; yy++) {
            float ypos = yy*cellSize+1;
            surf.drawLine(left, ypos, right, ypos, LINE_WIDTH);
        }

        // draw horizontal grid lines
        for (int xx = 0; xx <= game.boardSize; xx++) {
            float xpos = xx*cellSize+1;
            surf.drawLine(xpos, top, xpos, bot, LINE_WIDTH);
        }
    }

    /** Returns the offset to center of cell {@code cc} (in x or y) */
    public float cell (int cc) {
        // cc*cellSize is the upper left corner, then cellSize/2 to center,
        // then 1 to account for our 2 pixel line width.
        return cc*cellSize + cellSize/2 + 1;
    }
}
