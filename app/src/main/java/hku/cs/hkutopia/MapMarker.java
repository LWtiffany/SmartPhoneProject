package hku.cs.hkutopia;

import android.graphics.Bitmap;

public class MapMarker {
    private float x;
    private float y;
    private Bitmap bitmap;
    private Object tag;

    public MapMarker(float x, float y, Bitmap bitmap, Object tag) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
        this.tag = tag;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Object getTag() {
        return tag;
    }
}
