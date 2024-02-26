package de.tadris.fitness.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Spinner;

public class VerticalSpinner extends Spinner {

    private int _width, _height;
    private final Rect _bounds = new Rect();

    public VerticalSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setRotation(90);
    }

    public VerticalSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRotation(90);
    }

    public VerticalSpinner(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        // vise versa
        _height = getMeasuredWidth();
        _width = getMeasuredHeight();
        setMeasuredDimension(_height, _width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}