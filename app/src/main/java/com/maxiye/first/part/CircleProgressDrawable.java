package com.maxiye.first.part;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.maxiye.first.util.BitmapUtil;

/**
 * 自定义环形进度条
 * Created by 91287 on 2018/5/27.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CircleProgressDrawable extends Drawable {
    private Paint mPaint;
    private boolean gradual_flg = true;
    private String style = STYLE_SHADOW;
    public final static String STYLE_BORDER = "border";
    public final static String STYLE_SHADOW = "shadow";
    public final static String STYLE_NONE = "none";
    private int maxProgress = 100;
    private int curProgress = 0;
    private int circleWidth;
    private int circleColor = 0xFFF66725;
    private float percent;
    private RectF rectF;
    private int radius;
    private int pX;
    private int pY;

    private CircleProgressDrawable () {
        setBounds(0, 0, 90, 90);
        circleWidth = Math.min(getBounds().width(), getBounds().height()) >> 3;
    }
    //建造者模式
    public static class Builder {
        private final CircleProgressDrawable cpd;
        public Builder () {
            cpd = new CircleProgressDrawable();
        }

        public Builder color(int cirColor) {
            cpd.circleColor = cirColor;
            return this;
        }

        public Builder capacity(int maxProg) {
            cpd.maxProgress = maxProg;
            return this;
        }

        public Builder current(int curProg) {
            cpd.curProgress = curProg;
            return this;
        }

        public Builder gradual(boolean gradual) {
            cpd.gradual_flg = gradual;
            return this;
        }

        /**
         * 细度因子，越大越窄
         * @param divide 细度因子
         * @return builder
         */
        public Builder thin(int divide) {
            if (divide < 2) {
                divide = 2;
            }
            cpd.circleWidth = Math.min(cpd.getBounds().width(), cpd.getBounds().height()) / divide;
            return this;
        }

        public Builder style(String cirStyle) {
            cpd.style = cirStyle;
            return this;
        }

        public CircleProgressDrawable build() {
            cpd.init();
            return cpd;
        }
    }

    private void init() {
        mPaint = new Paint();
        // 描边
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置颜色
        mPaint.setColor(circleColor);
        // 设置抗锯齿
        mPaint.setAntiAlias(true);
        // 设置圆环宽度
        mPaint.setStrokeWidth(circleWidth);//往外侧增加一半，往内侧增加一半。
        // 设置圆角
        //mPaint.setStrokeCap(Paint.Cap.ROUND);
        percent = curProgress * 1.0f / maxProgress;
        final Rect bounds = getBounds();
        int width = bounds.width();int height = bounds.height();
        radius = (Math.min(width, height) - circleWidth) >> 1;
        // 1. 计算矩形位置.
        pX = width >> 1;
        pY = height >> 1;
        rectF = new RectF(pX - radius, pY - radius,pX + radius,pY + radius);
    }

    public void setStyle(String cirStyle) {
        this.style = cirStyle;
    }

    public void setMaxProgress(int maxProg) {
        maxProgress = maxProg;
    }

    public void setCurProgress(int cur) {
        curProgress = cur;
        percent = curProgress * 1.0f / maxProgress;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        mPaint.setColor(circleColor);
        //paint type
        switch (style) {
            case STYLE_NONE:
                break;
            case STYLE_BORDER:
                // 绘制圆环
                mPaint.setStrokeWidth(0.6f);
                canvas.drawCircle(pX, pY, radius + (circleWidth >> 1), mPaint);
                canvas.drawCircle(pX, pY, radius - (circleWidth >> 1), mPaint);
                mPaint.setStrokeWidth(circleWidth);
                break;
            case STYLE_SHADOW:
                // 绘制阴影
                mPaint.setAlpha(0x20);
                canvas.drawCircle(pX, pY, radius, mPaint);
                break;
        }
        // 计算角度.
        int angle = (int) (percent * 360);
        //颜色渐变
        if (gradual_flg) {
//            int Offset = (int) ((1- percent) * 150);
            mPaint.setColor(BitmapUtil.gradualColor(circleColor, (360 - angle)));
        } else {
            mPaint.setColor(circleColor);
        }
        // 2. 绘制进度条.
        canvas.drawArc(rectF, -90, angle, false, mPaint);
    }

    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }
}
