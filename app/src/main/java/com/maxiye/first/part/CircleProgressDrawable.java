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

/**
 * 自定义环形进度条
 * Created by 91287 on 2018/5/27.
 */
public class CircleProgressDrawable extends Drawable {
    private Paint mPaint;
    private int maxProgress;
    private int curProgress;
    private int circleWidth;
    private int circleColor;
    public CircleProgressDrawable(int maxProg, int cirColor) {
        maxProgress = maxProg;
        curProgress = 0;
        circleColor = cirColor;
        setBounds(0, 0, 90, 90);
        circleWidth = Math.min(getBounds().width(), getBounds().height()) / 10;
        mPaint = new Paint();
        // 描边
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置颜色
        mPaint.setColor(circleColor);
        // 设置抗锯齿
        mPaint.setAntiAlias(true);
        // 设置圆环宽度
        mPaint.setStrokeWidth(circleWidth);
        // 设置圆角
        //mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setCurProgress(int cur) {
        curProgress = cur;
        //颜色渐变
        int Offset = (int) ((1- curProgress * 1.0 / maxProgress) * (150));
        mPaint.setColor(circleColor + Offset);
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        final Rect bounds = getBounds();
        final int radius = (Math.min(bounds.width(), bounds.height()) - circleWidth * 2) / 2;
        // 计算矩形位置.
        final int offsetX = (bounds.width() - radius * 2)/2 ;
        final int offsetY = (bounds.height() - radius * 2)/2 ;
        RectF rect = new RectF(offsetX, offsetY,offsetX + radius * 2,offsetY + radius *2);
        // 计算角度.
        int angle = (int) ((curProgress * 1.0 / maxProgress) * 360);
        // 2. 绘制进度圆环.
        canvas.drawArc(rect, -90, angle, false, mPaint);
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
