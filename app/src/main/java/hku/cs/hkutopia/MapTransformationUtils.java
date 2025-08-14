package hku.cs.hkutopia;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Utility class for advanced map transformations including 3D-like effects
 */
public class MapTransformationUtils {

    /**
     * Calculate a 3D perspective transformation matrix
     * @param rotationX Rotation around X axis in degrees
     * @param rotationY Rotation around Y axis in degrees
     * @param pivotX X coordinate of the pivot point
     * @param pivotY Y coordinate of the pivot point
     * @return A transformation matrix with perspective effect
     */
    public static Matrix calculate3DTransformation(float rotationX, float rotationY, float pivotX, float pivotY) {
        // Create a camera and matrix
        android.graphics.Camera camera = new android.graphics.Camera();
        Matrix matrix = new Matrix();

        // Apply rotation
        camera.save();
        camera.rotateX(rotationX);
        camera.rotateY(rotationY);
        camera.getMatrix(matrix);
        camera.restore();

        // Adjust matrix for pivot point
        matrix.preTranslate(-pivotX, -pivotY);
        matrix.postTranslate(pivotX, pivotY);

        return matrix;
    }

    /**
     * Calculate the distance between two touch points
     * @param event Touch event with multiple pointers
     * @return Distance between first two pointers, or 0 if less than 2 pointers
     */
    public static float calculateDistance(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0;
        }

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Smoothly constrain a map view to boundaries
     * @param viewRect The rectangle of the view
     * @param mapRect The rectangle of the map after transformation
     * @param dampingFactor Damping factor for smoother movement (0-1)
     * @return Translation values [dx, dy] to apply
     */
    public static float[] calculateConstrainedPosition(RectF viewRect, RectF mapRect, float dampingFactor) {
        float dx = 0, dy = 0;

        // Calculate horizontal constraints
        if (mapRect.width() < viewRect.width()) {
            dx = (viewRect.width() - mapRect.width()) / 2 - mapRect.left;
        } else {
            if (mapRect.left > 0) {
                dx = -mapRect.left;
            } else if (mapRect.right < viewRect.width()) {
                dx = viewRect.width() - mapRect.right;
            }
        }

        // Calculate vertical constraints
        if (mapRect.height() < viewRect.height()) {
            dy = (viewRect.height() - mapRect.height()) / 2 - mapRect.top;
        } else {
            if (mapRect.top > 0) {
                dy = -mapRect.top;
            } else if (mapRect.bottom < viewRect.height()) {
                dy = viewRect.height() - mapRect.bottom;
            }
        }

        // Apply damping for smooth movement
        return new float[] {dx * dampingFactor, dy * dampingFactor};
    }

    /**
     * Calculate marker position on the map
     * @param markerX Normalized X coordinate (0-1000)
     * @param markerY Normalized Y coordinate (0-1000)
     * @param mapWidth Width of the map bitmap
     * @param mapHeight Height of the map bitmap
     * @return Pixel coordinates [x, y] on the map
     */
    public static float[] calculateMarkerPosition(float markerX, float markerY, int mapWidth, int mapHeight) {
        return new float[] {
                (markerX / 1000f) * mapWidth,
                (markerY / 1000f) * mapHeight
        };
    }
}