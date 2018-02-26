package com.soda.lib;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Author : yr
 * Create at : 18-2-26 下午1:54.
 * Description :
 */

public class MagicImageView extends AppCompatImageView implements
        View.OnTouchListener,
        ScaleGestureDetector.OnScaleGestureListener,
        ViewTreeObserver.OnGlobalLayoutListener{

    private final Matrix mMatrix = new Matrix();
    private boolean mLayoutOnce;
    private static final float MAX_SCALE_FACTOR = 2.f;
    private static float MIN_SCALE_FACTOR = .5f;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mInitScaleFactor = 1.f;

    public MagicImageView(Context context) {
        this(context, null, 0);
    }

    public MagicImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagicImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
        setOnTouchListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    @Override
    public void onGlobalLayout() {
        if (mLayoutOnce)
            return;
        Drawable drawable = getDrawable();
        int width = getWidth();
        int height = getHeight();
        int intrinsicWidth = 0;
        int intrinsicHeight = 0;
        if (drawable != null) {
            intrinsicWidth = drawable.getIntrinsicWidth();
            intrinsicHeight = drawable.getIntrinsicHeight();
        }
        if (intrinsicWidth > width && intrinsicHeight > height) {
            mInitScaleFactor = Math.min(width*1.f/intrinsicWidth, height*1.f/intrinsicHeight);
        } else {
            if (intrinsicWidth >= width && intrinsicHeight <= height) {
                mInitScaleFactor = width*1.f/intrinsicWidth;
            } else if (intrinsicWidth <= width && intrinsicHeight >= height) {
                mInitScaleFactor = height*1.f/intrinsicHeight;
            }
        }
        int transX = (int) ((width - intrinsicWidth)/2.f + 0.5f);
        int transY = (int) ((height - intrinsicHeight)/2.f + 0.5f);
        mMatrix.postTranslate(transX, transY);
        mMatrix.postScale(mInitScaleFactor, mInitScaleFactor, getWidth()/2f, getHeight()/2f);
        MIN_SCALE_FACTOR = MIN_SCALE_FACTOR * mInitScaleFactor;
        setImageMatrix(mMatrix);
        mLayoutOnce = true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getCurrentScale();
        float scaleFactor = detector.getScaleFactor();
        if (getDrawable() != null) {
            if ((scale <= MAX_SCALE_FACTOR && scaleFactor >= 1.f)
                    || (scale >= MIN_SCALE_FACTOR && scaleFactor <= 1.f)) {
                if (scale*scaleFactor < MIN_SCALE_FACTOR) {
                    scaleFactor = MIN_SCALE_FACTOR/scale;
                }
                if (scale*scaleFactor > MAX_SCALE_FACTOR) {
                    scaleFactor = MAX_SCALE_FACTOR/scale;
                }
                mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(mMatrix);
            }
        }
        return true;
    }

    private float getCurrentScale() {
        float[] matrix = new float[9];
        mMatrix.getValues(matrix);
        return matrix[Matrix.MSCALE_X];
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mScaleGestureDetector.onTouchEvent(event);
    }
}
