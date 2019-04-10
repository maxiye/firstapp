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
import android.support.v4.graphics.ColorUtils;

/**
 * 自定义环形进度条
 * {@code 第2条：遇到多个构造器参数时，考虑用构建者}
 * {@code 第3条：使用私有构造器或者枚举类型来强化Singleton属性}
 * {@code 第6条：避免创建不必要的对象}
 * {@code 第23条：优先使用类层次，而不是标签类}
 * {@code 第40条：坚持使用{@link Override}注解}
 * @author due
 * @date 2018/5/27
 */
public class CircleProgressDrawable extends Drawable {
    private Paint mPaint;
    private int maxProgress = 100;
    private int curProgress = 0;
    private int circleWidth;
    private int startColor = 0xFFF66725;
    private int endColor = 0xFF009688;
    private float percent;
    private RectF rectF;
    private int radius, pX, pY;

    private CircleProgressDrawable () {
        setBounds(0, 0, 90, 90);
        circleWidth = Math.min(getBounds().width(), getBounds().height()) >> 3;
    }

    /**
     * 建造者模式
     */
    public static class Builder {
        private final CircleProgressDrawable cpd;
        public Builder () {
            cpd = new CircleProgressDrawable();
        }

        public Builder color(int start, int end) {
            cpd.startColor = start;
            cpd.endColor = end;
            return this;
        }

        public Builder threshold(int threshold) {
            cpd.maxProgress = threshold;
            return this;
        }

        @SuppressWarnings({"unused"})
        public Builder progress(int progress) {
            cpd.curProgress = progress;
            return this;
        }

        /**
         * 细度因子，越大越窄
         * @param divide 细度因子
         * @return builder
         */
        @SuppressWarnings({"unused"})
        public Builder thin(int divide) {
            int minDivide = 2;
            if (divide < minDivide) {
                divide = minDivide;
            }
            cpd.circleWidth = Math.min(cpd.getBounds().width(), cpd.getBounds().height()) / divide;
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
        mPaint.setColor(endColor);
        // 设置抗锯齿
        mPaint.setAntiAlias(true);
        // 设置圆环宽度
        // 往外侧增加一半，往内侧增加一半。
        mPaint.setStrokeWidth(circleWidth);
        // 设置圆角
        /*mPaint.setStrokeCap(Paint.Cap.ROUND);*/
        percent = curProgress * 1.0f / maxProgress;
        final Rect bounds = getBounds();
        int width = bounds.width();int height = bounds.height();
        radius = (Math.min(width, height) - circleWidth) >> 1;
        // 1. 计算矩形位置.
        pX = width >> 1;
        pY = height >> 1;
        rectF = new RectF(pX - radius, pY - radius,pX + radius,pY + radius);
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
        mPaint.setColor(endColor);
        //paint type.
        mPaint.setAlpha(0x20);
        canvas.drawCircle(pX, pY, radius, mPaint);
        // 颜色渐变.
        mPaint.setColor(ColorUtils.blendARGB(startColor, endColor, percent));
        // 计算角度.
        int angle = (int) (percent * 360);
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


    /**
     * 绘制圆环
     * @param canvas Canvas
     */
    @SuppressWarnings({"unused"})
    private void drawBorder(@NonNull Canvas canvas) {
        mPaint.setStrokeWidth(0.6f);
        canvas.drawCircle(pX, pY, radius + (circleWidth >> 1), mPaint);
        canvas.drawCircle(pX, pY, radius - (circleWidth >> 1), mPaint);
        mPaint.setStrokeWidth(circleWidth);
    }

    /**
     * 绘制阴影
     * @param canvas Canvas
     */
    @SuppressWarnings({"unused"})
    private void drawShadow(@NonNull Canvas canvas) {
        mPaint.setAlpha(0x20);
        canvas.drawCircle(pX, pY, radius, mPaint);
    }

}
