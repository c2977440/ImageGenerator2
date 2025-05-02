package com.example.larp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class VerticalTextView extends TextView {
    private static final String TAG = "VerticalTextView";
    private boolean isVertical = true;
    private Paint paint;
    private Rect textBounds;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;
    private static final float MIN_WIDTH = 100f;
    private static final float MIN_HEIGHT = 100f;

    public VerticalTextView(Context context) {
        super(context);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        textBounds = new Rect();
        setClickable(true);
        setFocusable(true);
    }

    public void setVerticalText(boolean vertical) {
        isVertical = vertical;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isVertical) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        String text = getText() != null ? getText().toString() : "";
        if (text.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        paint.setTextSize(getTextSize());
        paint.setTypeface(getTypeface());

        // 計算最大字符寬度
        float maxCharWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            String currentChar = String.valueOf(text.charAt(i));
            float charWidth = paint.measureText(currentChar);
            maxCharWidth = Math.max(maxCharWidth, charWidth);
        }

        // 計算總高度
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float textHeight = textBounds.height();
        float spacing = getTextSize() * 0.2f;
        float totalHeight = (textHeight + spacing) * text.length();

        // 添加內邊距
        int width = (int) (maxCharWidth + getPaddingLeft() + getPaddingRight());
        int height = (int) (totalHeight + getPaddingTop() + getPaddingBottom());

        // 確保最小尺寸
        width = Math.max(width, (int) MIN_WIDTH);
        height = Math.max(height, (int) MIN_HEIGHT);

        setMeasuredDimension(
            resolveSize(width, widthMeasureSpec),
            resolveSize(height, heightMeasureSpec)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isVertical) {
            super.onDraw(canvas);
            return;
        }

        String text = getText().toString();
        if (text == null || text.isEmpty()) {
            return;
        }

        paint.reset();
        paint.setAntiAlias(true);
        paint.setTextSize(getTextSize());
        paint.setColor(getCurrentTextColor());
        paint.setTypeface(getTypeface());

        // 計算文字高度
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float textHeight = textBounds.height();
        float spacing = getTextSize() * 0.2f;

        // 計算起始位置
        float startX = getWidth() / 2f;
        float startY = getPaddingTop() + textHeight;

        // 繪製每個字符
        for (int i = 0; i < text.length(); i++) {
            String currentChar = String.valueOf(text.charAt(i));
            float charWidth = paint.measureText(currentChar);
            float x = startX - (charWidth / 2);
            float y = startY + (i * (textHeight + spacing));
            canvas.drawText(currentChar, x, y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                isDragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float deltaX = event.getRawX() - lastTouchX;
                    float deltaY = event.getRawY() - lastTouchY;
                    
                    setX(getX() + deltaX);
                    setY(getY() + deltaY);
                    
                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }
}
