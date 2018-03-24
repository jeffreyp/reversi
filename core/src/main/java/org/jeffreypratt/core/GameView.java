package org.jeffreypratt.core;

import playn.core.Canvas;
import playn.core.Sound;
import playn.core.Texture;
import playn.core.Tile;
import playn.scene.*;
import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;
import pythagoras.f.Point;
import react.RMap;
import tripleplay.anim.Animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView extends GroupLayer {
    private final Reversi game;
    private final BoardView bview;
    private final GroupLayer pgroup = new GroupLayer();
    private final Tile[] ptiles = new Tile[Reversi.Piece.values().length];
    private final Map<Reversi.Coord, ImageLayer> pviews = new HashMap<>();
    private final Sound click;
    private final FlipBatch flip;

    private ImageLayer addPiece (Reversi.Coord at, Reversi.Piece piece) {
        ImageLayer pview = new ImageLayer(ptiles[piece.ordinal()]);
        pview.setOrigin(Layer.Origin.CENTER);
        pgroup.addAt(pview, bview.cell(at.x), bview.cell(at.y));
        return pview;
    }

    private void setPiece (Reversi.Coord at, Reversi.Piece piece) {
        ImageLayer pview = pviews.get(at);
        if (pview == null) {
            pviews.put(at, pview = addPiece(at, piece));
            // animate the piece view "falling" into place
            pview.setVisible(false).setScale(2);
            game.anim.setVisible(pview, true).then().tweenScale(pview).to(1).in(500).bounceOut();
            game.anim.delay(250).then().play(click);
            game.anim.addBarrier();
        } else {
            //pview.setTile(ptiles[piece.ordinal()]);
            final ImageLayer fview = pview;
            final Tile tile = ptiles[piece.ordinal()];
            final Point eye = LayerUtil.layerToScreen(pview, fview.width()/2, fview.height()/2);
            Animation.Value flipAngle = new Animation.Value() {
                public float initial () { return flip.angle; }
                public void set (float value) { flip.angle = value; }
            };
            game.anim.
                    action(new Runnable() { public void run () {
                        flip.eyeX = eye.x;
                        flip.eyeY = eye.y;
                        fview.setBatch(flip);
                    }}).
                    then().tween(flipAngle).from(0).to(FloatMath.PI/2).in(150).
                    then().action(new Runnable() { public void run () {
                        fview.setTile(tile);
                    }}).
                    then().tween(flipAngle).to(FloatMath.PI).in(150).
                    then().action(new Runnable() { public void run () {
                        fview.setBatch(null);
                    }});
            game.anim.addBarrier();
        }
    }

    private void clearPiece (Reversi.Coord at) {
        ImageLayer pview = pviews.remove(at);
        if (pview != null) pview.close();
    }

    public void showPlays (List<Reversi.Coord> coords, final Reversi.Piece color) {
        final List<ImageLayer> plays = new ArrayList<>();
        for (final Reversi.Coord coord : coords) {
            ImageLayer pview = addPiece(coord, color);
            // fade in the piece
            //pview.setAlpha(0);
            //game.anim.tweenAlpha(pview).to(0.3f).in(300);
            pview.setVisible(false).setAlpha(0);
            game.anim.setVisible(pview, true).then().tweenAlpha(pview).to(0.3f).in(300);
            // when the player clicks on a potential play, commit that play as their move.
            pview.events().connect(new Pointer.Listener() {
                @Override public void onStart (Pointer.Interaction iact) {
                    // clear out the potential plays layers
                    for (ImageLayer play : plays) play.close();
                    // apply this play to the game state
                    game.logic.applyPlay(game.pieces, color, coord);
                    // and move to the next player's turn
                    game.turn.update(color.next());
                }
            });

            // when the player hovers over a potential play, highlight it.
            pview.events().connect(new Mouse.Listener() {
                @Override public void onHover (Mouse.HoverEvent event, Mouse.Interaction iact) {
                    iact.hitLayer.setAlpha(event.inside ? 0.6f : 0.3f);
                }
            });

            plays.add(pview);
        }
    }

    public GameView (Reversi game, IDimension viewSize) {
        this.game = game;
        this.bview = new BoardView(game, viewSize);
        this.click = game.plat.assets().getSound("sounds/click");
        this.flip = new FlipBatch(game.plat.graphics().gl, 2);
        addCenterAt(bview, viewSize.width() / 2, viewSize.height() / 2);
        addAt(pgroup, bview.tx(), bview.ty());

        // draw a black piece and a white piece into a single canvas image
        float size = bview.cellSize - 2, hsize = size / 2;
        Canvas canvas = game.plat.graphics().createCanvas(2 * size, size);
        canvas.setFillColor(0xFF000000).fillCircle(hsize, hsize, hsize).setStrokeColor(0xFFFFFFFF).
                setStrokeWidth(2).strokeCircle(hsize, hsize, hsize - 1);
        canvas.setFillColor(0xFFFFFFFF).fillCircle(size + hsize, hsize, hsize).setStrokeColor(0xFF000000).
                setStrokeWidth(2).strokeCircle(size + hsize, hsize, hsize - 1);

        // convert the image to a texture and extract a texture region (tile) for each piece
        Texture ptex = canvas.toTexture(Texture.Config.UNMANAGED);
        ptiles[Reversi.Piece.BLACK.ordinal()] = ptex.tile(0, 0, size, size);
        ptiles[Reversi.Piece.WHITE.ordinal()] = ptex.tile(size, 0, size, size);

        // dispose our pieces texture when this layer is disposed
        onDisposed(ptex.disposeSlot());

        game.pieces.connect(new RMap.Listener<Reversi.Coord, Reversi.Piece>() {
            @Override public void onPut (Reversi.Coord coord, Reversi.Piece piece) { setPiece(coord, piece); }
            @Override public void onRemove (Reversi.Coord coord) { clearPiece(coord); }
        });
    }

    @Override
    public void close () {
        super.close();
        flip.close();
        ptiles[0].texture().close(); // both ptiles reference the same texture
    }
}
