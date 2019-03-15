package com.example.campuspaths;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;

import hw5.Edge;
import hw8.Coordinate;

/**
 * Custom DrawView that allows for painting on an ImageView.
 */
public class DrawView extends AppCompatImageView {
    public static final float SCALING = 0.23f;

    static Paint paint = new Paint();
    static int routeColor = Color.rgb(255, 255, 255);
    static int srcColor = Color.rgb(85, 239, 196);
    static int dstColor = Color.rgb(232, 67, 147);
    static float radius = 10.0f;

    // Coordinates in raw units.
    private Coordinate src = null;
    private Coordinate dst = null;
    private float[] pts = null;

    public DrawView(Context context) {
        super(context);
    }
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * onDraw.
     * This is the primary entrance for controlling the drawing behavior for the base ImageView.
     * @param canvas Canvas to draw on the base ImageView. */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.printf("DrawView: onDraw: " +
                "\n\tsrc: '%s'" +
                "\n\tdst: '%s'" +
                "\n\tpts: '%s'\n",
                src, dst, Arrays.toString(pts));

        if (pts != null) {
            this.drawPath(canvas, pts);
        }
        else {
            System.out.println("DrawView: onDraw: pts is null!");
        }

        if (src != null) {
            drawCircle((float) src.getX(), (float) src.getY(), srcColor, canvas);
        }
        else {
            System.out.println("DrawView: onDraw: src is null!");
        }

        if (dst != null) {
            drawCircle((float) dst.getX(), (float) dst.getY(), dstColor, canvas);
        }
        else {
            System.out.println("DrawView: onDraw: src is null!");
        }
    }

    /**
     * Drawer.
     * Draws a circle on the canvas using the static Paint object.
     * @param x float x-coordinate to draw the center of the circle.
     * @param y float y-coordinate to draw the center of the circle.
     * @param color Color to draw the circle.
     * @param canvas Canvas to draw on.
     * @spec.requires paint is not null and color is in terms of color units.
     * @spec.effects Draws a circle at the given (x, y) coordinate on the canvas using paint. */
    private void drawCircle(float x, float y, int color, Canvas canvas) {
        System.out.printf("DrawView: drawCircle: " +
                "\n\tx: '%s'" +
                "\n\ty: '%s'" +
                "\n\tcolor: '%s'\n",
                x, y, color);
        paint.setColor(color);
        canvas.drawCircle(x, y, radius, paint);
    }

    /**
     * Drawer.
     * Draws a set of lines on the canvas using the static Paint object.
     * @param canvas Canvas to draw on.
     * @param pts List of (x0, y0, x1, y1, ..., xn, yn) float points
     * @spec.requires pts must not be null and must have even length.
     * @spec.effects Draws a circle at the given (x, y) coordinate on the canvas using paint. */
    private void drawPath(Canvas canvas, float[] pts) {
        System.out.printf("DrawView: drawPath:" +
                "\n\tpts: '%s'" +
                "\n\tsrc: '%s'" +
                "\n\tdst: '%s'\n",
                Arrays.toString(pts), src, dst);
        assert(pts != null && pts.length % 2 == 0);
        canvas.drawLines(pts, paint);
    }

    /**
     * Setter.
     * This sets the pts for the map to draw given a list of paths.
     * @param path ArrayList of Edge&lt;Double, Coordinate&gt; representing the path.
     * @spec.requires path is not null and has length divisible by 4.
     * @spec.effects This repopulates pts, and invalidates the state to force the view to re-draw. */
    public void setPts(ArrayList<Edge<Double, Coordinate>> path) {
        System.out.printf("DrawView: setPts: \n\tpath: '%s'\n", Arrays.toString(new ArrayList[]{path}));
        if (path != null) {
            paint.setColor(routeColor);

            // Inv: For each path_i, pts contains [..., path_i_src_x, path_i_src_y, path_i_dst_x, path_i_dst_y].
            pts = new float[path.size() * 4];
            for (int i = 0; i < path.size(); i++) {
                // 0 --> [0, 1, 2, 3]; 1 --> [4, 5, 6, 7], ..., n --> [4n, 4n+1, 4n+2, 4n+3]
                int j = i * 4;
                pts[j] = (float) path.get(i).getSrc().getName().getX() * SCALING;
                pts[j+1] = (float) path.get(i).getSrc().getName().getY() * SCALING;
                pts[j+2] = (float) path.get(i).getDst().getName().getX() * SCALING;
                pts[j+3] = (float) path.get(i).getDst().getName().getY() * SCALING;
            }
        }
        else {
            pts = null;
        }
        this.invalidate();
    }

    /**
     * Setter.
     * This sets the source coordinate in image-space coordinates.
     * @param src Coordinate in raw-space coordinates to set as the source coordinate.
     * @spec.requires src Coordinate is in raw-space if not null.
     * @spec.effects This re-sets src, and invalidates the state to force the view to re-draw. */
    public void setSrc(Coordinate src) {
        System.out.printf("DrawView: setSrc: \n\tsrc: '%s'\n", src);
        if (src != null) {
            this.src = new Coordinate(SCALING * src.getX(), SCALING * src.getY());
        }
        else {
            this.src = null;
        }
        this.invalidate();
    }

    /**
     * Setter.
     * This sets the destination coordinate in image-space coordinates.
     * @param dst Coordinate in raw-space coordinates to set as the destination coordinate.
     * @spec.requires dst Coordinate is not null and is in raw-space.
     * @spec.effects This re-sets dst, and invalidates the state to force the view to re-draw. */
    public void setDst(Coordinate dst) {
        System.out.printf("DrawView: setDst: \n\tdst: '%s'\n", dst);
        if (dst != null) {
            this.dst = new Coordinate(SCALING * dst.getX(), SCALING * dst.getY());
        }
        else {
            this.dst = null;
        }
        this.invalidate();
    }

    /**
     * Getter.
     * This returns a copy Coordinate of the source. Returns null if is null.
     * @return a copy Coordinate of the source. Returns null if is null. */
    public Coordinate getSrc() {
        if (this.src == null) {
            return null;
        }
        return new Coordinate(this.src.getX(), this.src.getY());
    }

    /**
     * Getter.
     * This returns a copy Coordinate of the destination. Returns null if is null.
     * @return a copy Coordinate of the destination. Returns null if is null. */
    public Coordinate getDst() {
        if (this.dst == null) {
            return null;
        }
        return new Coordinate(this.dst.getX(), this.dst.getY());
    }
}
