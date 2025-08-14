package hku.cs.hkutopia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class CustomMapView extends View {

    private Bitmap mapBitmap;
    private Matrix transformMatrix;
    private float scaleFactor = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float rotationDegrees = 0f;
    private float perspectiveX = 0f;
    private float perspectiveY = 0f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private Paint paint;
    private Paint markerShadowPaint;
    private Paint highlightPaint;

    private List<MapMarker> markers = new ArrayList<>();
    private OnMarkerClickListener markerClickListener;
    private OnMapClickListener mapClickListener;

    // For double tap zoom
    private static final float DOUBLE_TAP_ZOOM_FACTOR = 1.5f;

    // For limiting pan and zoom
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 4.0f;
    private RectF viewRect;
    private RectF mapRect;

    // For 3D effect
    private android.graphics.Camera camera;
    private Matrix cameraMatrix;
    private float maxPerspectiveDistortion = 10.0f;
    private Interpolator perspectiveInterpolator;

    // For edge glow effect
    private Paint edgeGlowPaint;
    private float edgeGlowIntensity = 0f;
    private static final float MAX_EDGE_GLOW = 0.5f;

    // For marker animation
    private Interpolator bounceInterpolator;
    private float markerPulsePhase = 0f;
    private float markerBounceAmplitude = 0.12f; // 增加弹性振幅
    private float markerBounceFrequency = 1.8f;  // 调整弹性频率
    private long lastFrameTime;

    public CustomMapView(Context context) {
        super(context);
        init(context);
    }

    public CustomMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Initialize matrices and camera for 3D effects
        transformMatrix = new Matrix();
        cameraMatrix = new Matrix();
        camera = new android.graphics.Camera();
        perspectiveInterpolator = new AccelerateDecelerateInterpolator();

        // Initialize paints
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        // Initialize marker shadow paint
        markerShadowPaint = new Paint();
        markerShadowPaint.setAntiAlias(true);
        markerShadowPaint.setColor(Color.BLACK);
        markerShadowPaint.setAlpha(80);
        markerShadowPaint.setStyle(Paint.Style.FILL);

        // Initialize highlight paint for selected markers
        highlightPaint = new Paint();
        highlightPaint.setAntiAlias(true);
        highlightPaint.setColor(Color.WHITE);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(4f);

        // Initialize edge glow paint
        edgeGlowPaint = new Paint();
        edgeGlowPaint.setAntiAlias(true);
        edgeGlowPaint.setStyle(Paint.Style.FILL);

        // Load the HKU campus map as a bitmap
        Drawable mapDrawable = ContextCompat.getDrawable(context, R.drawable.campus_map);
        if (mapDrawable != null) {
            int width = mapDrawable.getIntrinsicWidth();
            int height = mapDrawable.getIntrinsicHeight();
            mapBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mapBitmap);
            mapDrawable.setBounds(0, 0, width, height);
            mapDrawable.draw(canvas);
        }

        // Set up gesture detectors
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        viewRect = new RectF();
        mapRect = new RectF();

        // Initialize animation timing
        lastFrameTime = System.currentTimeMillis();

        // Enable hardware acceleration for better performance
        // 使用软件渲染模式来避免 SurfaceFlinger 事务失败
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        bounceInterpolator = new android.view.animation.OvershootInterpolator(2.0f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mapBitmap != null) {
            // Calculate initial scale to fit the map to the view
            float scaleX = (float) w / mapBitmap.getWidth();
            float scaleY = (float) h / mapBitmap.getHeight();
            float initialScale = Math.min(scaleX, scaleY) * 0.9f; // Slightly smaller for better effect

            // Center the map
            float dx = (w - mapBitmap.getWidth() * initialScale) / 2;
            float dy = (h - mapBitmap.getHeight() * initialScale) / 2;

            transformMatrix.reset();
            transformMatrix.postScale(initialScale, initialScale);
            transformMatrix.postTranslate(dx, dy);

            // Update scale factor
            scaleFactor = initialScale;
            translateX = dx;
            translateY = dy;

            // Update view and map rectangles
            viewRect.set(0, 0, w, h);
            updateMapRect();

            // Add initial 3D perspective effect
            applyPerspectiveEffect(5f, 0f);

            // Start animation
            postInvalidateOnAnimation();
        }
    }

    private void updateMapRect() {
        if (mapBitmap != null) {
            mapRect.set(0, 0, mapBitmap.getWidth(), mapBitmap.getHeight());
            transformMatrix.mapRect(mapRect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 更新动画时间 - 使用更精确的时间计算
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastFrameTime) / 1000f, 0.05f); // 限制最大时间步长为50ms
        lastFrameTime = currentTime;

        // 更平滑的脉冲动画
        markerPulsePhase += deltaTime * markerBounceFrequency; // 控制动画速度
        if (markerPulsePhase > 2 * Math.PI) {
            markerPulsePhase -= 2 * Math.PI;
        }

        // 绘制地图时使用更高效的变换
        if (mapBitmap != null) {
            // 预先计算组合变换矩阵
            Matrix combinedMatrix = new Matrix(transformMatrix);
            combinedMatrix.postConcat(cameraMatrix);

            // 使用优化的绘制方法
            canvas.drawBitmap(mapBitmap, combinedMatrix, paint);

            // 优化边缘发光效果
            if (edgeGlowIntensity > 0) {
                drawEdgeGlow(canvas);
            }
        }

        // 在onDraw方法中标记绘制部分
        for (MapMarker marker : markers) {
            if (marker.getBitmap() != null) {
                // 计算标记位置
                float[] point = new float[]{
                        (marker.getX() / 1000f) * mapBitmap.getWidth(),
                        (marker.getY() / 1000f) * mapBitmap.getHeight()
                };

                // 应用变换
                Matrix combinedMatrix = new Matrix(transformMatrix);
                combinedMatrix.postConcat(cameraMatrix);
                combinedMatrix.mapPoints(point);

                // 使用更Q弹的缩放动画
                // 为每个标记添加轻微的相位差，使动画不同步
                float markerPhase = markerPulsePhase + (marker.getX() * 0.003f + marker.getY() * 0.005f);
                // 使用弹性函数计算缩放因子
                float bounce = (float) (Math.sin(markerPhase) * markerBounceAmplitude);
                // 添加二次弹性效果
                float secondaryBounce = (float) (Math.sin(markerPhase * 2.5f) * (markerBounceAmplitude * 0.3f));
                // 组合主要和次要弹性效果
                float markerScale = 1.0f + bounce + secondaryBounce;

                float markerWidth = marker.getBitmap().getWidth() * markerScale;
                float markerHeight = marker.getBitmap().getHeight() * markerScale;

                // 优化绘制位置计算，添加轻微的上下浮动效果
                float verticalOffset = (float) (Math.sin(markerPhase * 1.2f) * 3.0f);
                float x = point[0] - markerWidth / 2f;
                float y = point[1] - markerHeight + verticalOffset;

                // 使用优化的绘制方法
                canvas.drawBitmap(marker.getBitmap(), null,
                        new RectF(x, y, x + markerWidth, y + markerHeight), paint);

                // 优化高亮效果，使其与弹性动画同步
                float highlightIntensity = (float) Math.abs(Math.sin(markerPhase * 0.8f) * 0.5f + 0.3f);
                highlightPaint.setAlpha((int) (highlightIntensity * 80));
                canvas.drawCircle(point[0], point[1] - markerHeight / 2,
                        markerWidth / 3 * (0.8f + highlightIntensity * 0.2f), highlightPaint);
            }
        }

        // 使用更温和的动画循环策略，减少系统负担
        postInvalidateDelayed(16); // 约60fps，但给系统留出更多处理时间
    }

    private void drawEdgeGlow(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // Create gradient for edge glow
        int glowColor = Color.argb(
                (int) (edgeGlowIntensity * 255 * 0.7f),
                255, 100, 100);

        // Top edge glow
        edgeGlowPaint.setShader(new LinearGradient(
                0, 0, 0, height * 0.15f,
                glowColor, Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height * 0.15f, edgeGlowPaint);

        // Bottom edge glow
        edgeGlowPaint.setShader(new LinearGradient(
                0, height, 0, height * 0.85f,
                glowColor, Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, height * 0.85f, width, height, edgeGlowPaint);

        // Left edge glow
        edgeGlowPaint.setShader(new LinearGradient(
                0, 0, width * 0.15f, 0,
                glowColor, Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width * 0.15f, height, edgeGlowPaint);

        // Right edge glow
        edgeGlowPaint.setShader(new LinearGradient(
                width, 0, width * 0.85f, 0,
                glowColor, Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(width * 0.85f, 0, width, height, edgeGlowPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean scaleResult = scaleDetector.onTouchEvent(event);
        boolean gestureResult = gestureDetector.onTouchEvent(event);

        // Handle edge glow effect
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // Calculate how close we are to the edge
            float x = event.getX();
            float y = event.getY();
            float width = getWidth();
            float height = getHeight();

            float distanceFromEdge = Math.min(
                    Math.min(x, width - x),
                    Math.min(y, height - y)
            );

            float edgeThreshold = 100f;
            if (distanceFromEdge < edgeThreshold) {
                edgeGlowIntensity = (1 - distanceFromEdge / edgeThreshold) * MAX_EDGE_GLOW;
            } else {
                edgeGlowIntensity = Math.max(0, edgeGlowIntensity - 0.05f);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            edgeGlowIntensity = 0;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Check for marker clicks with improved hit detection
            float x = event.getX();
            float y = event.getY();

            // Apply perspective transformation to touch coordinates
            Matrix inverse = new Matrix();
            Matrix combinedMatrix = new Matrix(transformMatrix);
            combinedMatrix.postConcat(cameraMatrix);

            if (combinedMatrix.invert(inverse)) {
                float[] touchPoint = new float[]{x, y};
                inverse.mapPoints(touchPoint);

                // Find closest marker
                MapMarker closestMarker = null;
                float closestDistance = Float.MAX_VALUE;

                for (MapMarker marker : markers) {
                    // Calculate marker position in original map coordinates
                    float markerX = (marker.getX() / 1000f) * mapBitmap.getWidth();
                    float markerY = (marker.getY() / 1000f) * mapBitmap.getHeight();

                    // Calculate distance to touch point
                    float dx = touchPoint[0] - markerX;
                    float dy = touchPoint[1] - markerY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    // Adjust hit area based on scale factor
                    float hitRadius = 50f / scaleFactor;

                    if (distance < hitRadius && distance < closestDistance) {
                        closestMarker = marker;
                        closestDistance = distance;
                    }
                }

                if (closestMarker != null && markerClickListener != null) {
                    markerClickListener.onMarkerClick(closestMarker);
                    return true;
                } else if (mapClickListener != null) {
                    // If no marker was clicked, notify map click listener
                    mapClickListener.onMapClick();
                }
            }
        }

        return scaleResult || gestureResult || super.onTouchEvent(event);
    }

    public void addMarker(MapMarker marker) {
        markers.add(marker);
        invalidate();
    }

    public void clearMarkers() {
        markers.clear();
        invalidate();
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerClickListener = listener;
    }

    public void setOnMapClickListener(OnMapClickListener listener) {
        this.mapClickListener = listener;
    }

    // Scale gesture listener with enhanced 3D effects
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float focusX, focusY;
        private float initialSpan;
        private float initialScale;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            initialSpan = detector.getCurrentSpan();
            initialScale = CustomMapView.this.scaleFactor;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // 计算更精确的缩放因子，基于跨度变化
            float spanRatio = detector.getCurrentSpan() / initialSpan;

            // 使用插值器使缩放更平滑
            float interpolatedRatio = spanRatio;
            // 对于非常小的变化，使用更小的缩放步长
            if (Math.abs(spanRatio - 1.0f) < 0.05f) {
                interpolatedRatio = 1.0f + (spanRatio - 1.0f) * 0.5f;
            }

            float targetScale = initialScale * interpolatedRatio;

            // 限制缩放范围，但使用平滑的限制而不是硬性截断
            if (targetScale < MIN_SCALE) {
                float dampingFactor = 0.5f + 0.5f * (targetScale / MIN_SCALE);
                targetScale = MIN_SCALE + (targetScale - MIN_SCALE) * dampingFactor;
            } else if (targetScale > MAX_SCALE) {
                float dampingFactor = 0.5f + 0.5f * (MAX_SCALE / targetScale);
                targetScale = MAX_SCALE + (targetScale - MAX_SCALE) * dampingFactor;
            }

            // 计算相对于当前缩放的变化量
            float scaleFactor = targetScale / CustomMapView.this.scaleFactor;

            // 应用缩放变换，围绕焦点点
            transformMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);

            // 更新当前缩放
            CustomMapView.this.scaleFactor = targetScale;

            // 更新地图矩形
            updateMapRect();

            // 约束到视图
            constrainMap();

            // 根据缩放增强3D效果
            float perspectiveAmount = (CustomMapView.this.scaleFactor - MIN_SCALE) /
                    (MAX_SCALE - MIN_SCALE) * maxPerspectiveDistortion;
            applyPerspectiveEffect(perspectiveAmount, 0);

            invalidate();
            return true;
        }
    }

    // Gesture listener for panning and double tap with enhanced 3D effects
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Apply translation
            transformMatrix.postTranslate(-distanceX, -distanceY);

            // Update translation values
            translateX -= distanceX;
            translateY -= distanceY;

            // Update map rectangle
            updateMapRect();

            // Constrain to view
            constrainMap();

            // Apply perspective effect based on movement direction
            float maxPerspective = 15.0f;
            float perspectiveX = Math.min(Math.max(-distanceX / 10, -maxPerspective), maxPerspective);
            float perspectiveY = Math.min(Math.max(-distanceY / 10, -maxPerspective), maxPerspective);

            // Smoothly transition to new perspective
            CustomMapView.this.perspectiveX = perspectiveX * 0.3f + CustomMapView.this.perspectiveX * 0.7f;
            CustomMapView.this.perspectiveY = perspectiveY * 0.3f + CustomMapView.this.perspectiveY * 0.7f;

            invalidate();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float focusX = e.getX();
            float focusY = e.getY();

            // 计算目标缩放级别
            float targetScale;
            if (scaleFactor < (MIN_SCALE + MAX_SCALE) / 2) {
                // 如果当前缩放小于中间值，则放大到中间值
                targetScale = (MIN_SCALE + MAX_SCALE) / 2;
            } else if (scaleFactor < MAX_SCALE * 0.8f) {
                // 如果当前缩放小于最大值的80%，则放大到最大值
                targetScale = MAX_SCALE;
            } else {
                // 否则缩小到最小值
                targetScale = MIN_SCALE;
            }

            // 使用动画实现平滑缩放
            animateZoom(scaleFactor, targetScale, focusX, focusY);

            return true;
        }
    }

    // 添加新方法实现平滑缩放动画
    private void animateZoom(final float startScale, final float endScale, final float focusX, final float focusY) {
        final long duration = 300; // 动画持续时间（毫秒）
        final long startTime = System.currentTimeMillis();

        // 创建动画插值器
        final DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);

        // 创建动画更新器
        final Runnable animator = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = Math.min(1f, (float) elapsed / duration);
                float interpolatedT = interpolator.getInterpolation(t);

                // 计算当前缩放
                float currentScale = startScale + (endScale - startScale) * interpolatedT;
                float scaleChange = currentScale / scaleFactor;

                // 应用缩放
                transformMatrix.postScale(scaleChange, scaleChange, focusX, focusY);
                scaleFactor = currentScale;

                // 更新地图矩形
                updateMapRect();

                // 约束到视图
                constrainMap();

                // 应用透视效果
                float perspectiveAmount = (scaleFactor - MIN_SCALE) / (MAX_SCALE - MIN_SCALE) * maxPerspectiveDistortion;
                applyPerspectiveEffect(perspectiveAmount * (1 - interpolatedT) + 5f * interpolatedT, 0f);

                // 刷新视图
                invalidate();

                // 如果动画未完成，继续
                if (t < 1f) {
                    postDelayed(this, 16);
                } else {
                    // 动画结束后应用最终透视效果
                    applyPerspectiveEffect(5f, 0f);
                    invalidate();
                }
            }
        };

        // 开始动画
        post(animator);
    }

    // 应用更高效的透视变换
    void applyPerspectiveEffect(float rotationX, float rotationY) {
        camera.save();
        // 限制旋转角度以避免过度变形
        camera.rotateX(Math.min(Math.max(rotationX, -15), 15));
        camera.rotateY(Math.min(Math.max(rotationY, -15), 15));

        cameraMatrix.reset();
        camera.getMatrix(cameraMatrix);
        camera.restore();

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        cameraMatrix.preTranslate(-centerX, -centerY);
        cameraMatrix.postTranslate(centerX, centerY);
    }

    // 优化约束地图法
    private void constrainMap() {
        float dx = 0, dy = 0;

        // 优化居中逻辑
        if (mapRect.width() < viewRect.width()) {
            dx = (viewRect.width() - mapRect.width()) / 2 - mapRect.left;
        } else {
            if (mapRect.left > 0) {
                dx = -mapRect.left;
            } else if (mapRect.right < viewRect.width()) {
                dx = viewRect.width() - mapRect.right;
            }
        }

        if (mapRect.height() < viewRect.height()) {
            dy = (viewRect.height() - mapRect.height()) / 2 - mapRect.top;
        } else {
            if (mapRect.top > 0) {
                dy = -mapRect.top;
            } else if (mapRect.bottom < viewRect.height()) {
                dy = viewRect.height() - mapRect.bottom;
            }
        }

        // 使用更平滑的阻尼因子，根据距离边界的远近动态调整
        if (dx != 0 || dy != 0) {
            // 计算距离边界的距离
            float distanceX = (dx > 0) ? mapRect.left : (viewRect.width() - mapRect.right);
            float distanceY = (dy > 0) ? mapRect.top : (viewRect.height() - mapRect.bottom);

            // 根据距离计算阻尼因子
            float dampingFactorX = Math.min(1.0f, 0.8f + 0.2f * Math.abs(distanceX) / 100);
            float dampingFactorY = Math.min(1.0f, 0.8f + 0.2f * Math.abs(distanceY) / 100);

            transformMatrix.postTranslate(dx * dampingFactorX, dy * dampingFactorY);
            translateX += dx * dampingFactorX;
            translateY += dy * dampingFactorY;
            updateMapRect();
        }
    }

    // Interface for marker click events
    public interface OnMarkerClickListener {
        void onMarkerClick(MapMarker marker);
    }

    // Interface for map click events
    public interface OnMapClickListener {
        void onMapClick();
    }

    // Reset view transformations with enhanced animation
    public void resetView() {
        if (mapBitmap != null && getWidth() > 0 && getHeight() > 0) {
            // 计算初始缩放以适应视图
            float scaleX = (float) getWidth() / mapBitmap.getWidth();
            float scaleY = (float) getHeight() / mapBitmap.getHeight();
            float initialScale = Math.min(scaleX, scaleY) * 0.9f;

            // 计算居中位置
            float dx = (getWidth() - mapBitmap.getWidth() * initialScale) / 2;
            float dy = (getHeight() - mapBitmap.getHeight() * initialScale) / 2;

            // 应用戏剧性透视效果
            applyPerspectiveEffect(30f, 0f);
            invalidate();

            // 使用动画重置视图
            final float startScale = scaleFactor;
            final float endScale = initialScale;
            final float startX = translateX;
            final float startY = translateY;
            final float endX = dx;
            final float endY = dy;

            final long duration = 400; // 动画持续时间（毫秒）
            final long startTime = System.currentTimeMillis();

            // 创建动画插值器
            final DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);

            // 创建动画更新器
            final Runnable animator = new Runnable() {
                @Override
                public void run() {
                    long elapsed = System.currentTimeMillis() - startTime;
                    float t = Math.min(1f, (float) elapsed / duration);
                    float interpolatedT = interpolator.getInterpolation(t);

                    // 重置矩阵
                    transformMatrix.reset();

                    // 计算当前缩放和位置
                    float currentScale = startScale + (endScale - startScale) * interpolatedT;
                    float currentX = startX + (endX - startX) * interpolatedT;
                    float currentY = startY + (endY - startY) * interpolatedT;

                    // 应用变换
                    transformMatrix.postScale(currentScale, currentScale);
                    transformMatrix.postTranslate(currentX, currentY);

                    // 更新当前值
                    scaleFactor = currentScale;
                    translateX = currentX;
                    translateY = currentY;

                    // 更新地图矩形
                    updateMapRect();

                    // 应用透视效果
                    float perspectiveAmount = 30f * (1 - interpolatedT) + 5f * interpolatedT;
                    applyPerspectiveEffect(perspectiveAmount, 0f);

                    // 刷新视图
                    invalidate();

                    // 如果动画未完成，继续
                    if (t < 1f) {
                        postDelayed(this, 16);
                    } else {
                        // 动画结束后应用最终透视效果
                        applyPerspectiveEffect(3f, 0f);
                        invalidate();
                    }
                }
            };

            // 开始动画
            post(animator);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 释放资源
        if (camera != null) {
            camera = null;
        }
        // 清除标记和位图引用
        markers.clear();
        if (mapBitmap != null && !mapBitmap.isRecycled()) {
            mapBitmap.recycle();
            mapBitmap = null;
        }
        // 移除所有回调
        removeCallbacks(null);
    }
}