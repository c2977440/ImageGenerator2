package com.example.larp;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;

public class DraggableTextView extends AppCompatTextView {
    // 用於追蹤觸摸點與視圖左上角的偏移量
    private float dX = 0f;
    private float dY = 0f;

    // 用於追蹤上一次觸摸的位置
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;

    // 用於處理縮放手勢
    private ScaleGestureDetector scaleDetector;
    private boolean isScaling = false;
    private float scaleFactor = 1.0f;

    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 2.0f;
    private static final float MIN_TEXT_SIZE = 12f;
    private static final float MAX_TEXT_SIZE = 200f;
    private static final float MARGIN = 20f; // 邊界邊距

    private float lastX = 0f;
    private float lastY = 0f;
    private boolean isPositionSet = false;
    private boolean isDragging = false;

    public float getLastX() {
        return lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public void setLastPosition(float x, float y) {
        lastX = x;
        lastY = y;
        isPositionSet = true;
        setX(x);
        setY(y);
        Log.i("@date" , "@" + getTag() + "X:"  + x + " Y: " + y);
    }

    public boolean isPositionSet() {
        return isPositionSet;
    }

    // 建構函數
    public DraggableTextView(Context context) {
        super(context);
        init(context);
    }

    public DraggableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DraggableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                try {
                    float newScaleFactor = scaleFactor * detector.getScaleFactor();
                    float newTextSize = getTextSize() * detector.getScaleFactor();

                    // 檢查縮放限制
                    if (newScaleFactor >= MIN_SCALE && newScaleFactor <= MAX_SCALE &&
                            newTextSize >= MIN_TEXT_SIZE && newTextSize <= MAX_TEXT_SIZE) {
                        scaleFactor = newScaleFactor;
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isScaling = true;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            scaleDetector.onTouchEvent(event);

            if (isScaling) {
                return true;
            }

            ViewGroup parent = (ViewGroup) getParent();
            if (parent == null) return true;

            final float x = event.getRawX();
            final float y = event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = getX() - x;
                    dY = getY() - y;
                    lastTouchX = x;
                    lastTouchY = y;
                    isDragging = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) return true;

                    // 使用更精確的座標計算
                    float newX = x + dX;
                    float newY = y + dY;

                    // 增加移動範圍，允許文字部分超出父容器
                    float maxX = parent.getWidth() + getWidth() / 2;
                    float maxY = parent.getHeight() + getHeight() / 2;

                    // 允許負值，使文字可以完全移出左側和頂部
                    newX = Math.max(-getWidth() / 2, Math.min(newX, maxX));
                    newY = Math.max(-getHeight() / 2, Math.min(newY, maxY));

                    // 使用 setX/Y 來設置絕對位置
                    setX(newX);
                    setY(newY);
                    
                    // 更新最後的位置
                    lastX = newX;
                    lastY = newY;
                    isPositionSet = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    Log.i("@date" , "@" + getTag() + "X:"  + lastX + " Y: " + lastY);
                    break;

                case MotionEvent.ACTION_UP:
                    if (isDragging) {
                        float deltaX = x - lastTouchX;
                        float deltaY = y - lastTouchY;

                        if (Math.abs(deltaX) < 10 && Math.abs(deltaY) < 10) {
                            performClick();
                        }
                    }
                    isDragging = false;
                    break;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void performHapticFeedback() {
        try {
            performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY,
                    android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        } catch (Exception e) {
            // 忽略震動反饋錯誤
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 獲取文字內容
        String text = getText() != null ? getText().toString() : "";
        if (text.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // 計算文字所需的空間
        Paint paint = new Paint();
        paint.setTextSize(getTextSize());
        paint.setTypeface(getTypeface());

        // 計算文字寬度
        float textWidth = paint.measureText(text);
        
        // 計算文字高度
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = fm.bottom - fm.top;

        // 增加額外的內邊距，確保文字不會被裁切
        float extraPadding = getTextSize() * 0.5f; // 根據文字大小增加額外內邊距
        
        // 添加內邊距，但不限制最大高度
        int width = (int) (textWidth + getPaddingLeft() + getPaddingRight() + extraPadding * 2);
        int height = (int) (textHeight + getPaddingTop() + getPaddingBottom() + extraPadding * 2);

        // 確保最小尺寸，但不限制最大尺寸
        width = Math.max(width, (int) (getTextSize() * 2));  // 最小寬度為文字大小的兩倍
        height = Math.max(height, (int) (getTextSize() * 2)); // 最小高度為文字大小的兩倍

        // 使用 UNSPECIFIED 模式來允許視圖自由調整大小
        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED);
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);

        // 設定測量結果
        setMeasuredDimension(
            resolveSize(width, widthSpec),
            resolveSize(height, heightSpec)
        );
    }
}