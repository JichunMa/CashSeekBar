package com.example.majichun.cashseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;

/**
 * Created by jichunma
 * on 2017/11/17.
 * <p>
 * describe:
 */

public class CashSeekBar extends View {

  boolean isInit = false;
  private static final String TAG = "MineSeekBar";
  boolean debugMode = false;

  private Context context;

  private int thumbRadius;
  private int marginHorizontally;//水平间距
  private int tipBottomMargin;//提示层距下间距
  private int instructTextMarginTop;//指示文字距顶
  private boolean isShadowLayer = true;//滑块是否添加阴影

  // TODO: 2017/11/20 　目前写死的参数
  String startText = "200.00";
  String endText = "3000.00";
  String tipDisplayText = "";//提示层展示的内容

  int tipInnerPadding;//提示文字的内间距
  int triangleHeight;


  private int axisStart;//坐标轴起点
  private int axisEnd;//坐标轴终点

  //滑块中心坐标
  private int thumbCenterX = -1;

  float percentage = -1.0f;

  OnProgressChangeListener listener;

  private int progress;//进度

  int axisColor = Color.parseColor("#BDBDBD");//坐标轴颜色
  int axisProgressColor = Color.parseColor("#60C99D");//进度条颜色

  private int axisLineHeight;//坐标轴高度

  //画布宽高
  private int canvasHeight;
  private int canvasWidth;


  private int maxProgressLength;//进度条总长度
  private int currentProgressLength;//进度条当前长度


  Paint mPaintLine = new Paint();
  Paint mPaintThumb = new Paint();
  Paint mPaintTestLine = new Paint();
  Paint mPaintTriangle = new Paint();
  Paint mPaintText = new Paint();


  public CashSeekBar(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    this.context = context;
    initPaint(context);
    initParam(context);
    TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.MineSeekBar);
    percentage = t.getFloat(R.styleable.MineSeekBar_percentage, percentage);
    t.recycle();
  }

  public CashSeekBar(Context context) {
    this(context, null);
  }


  private void initPaint(Context context) {
    mPaintLine.setAntiAlias(true);
    mPaintLine.setStyle(Paint.Style.FILL);
    mPaintLine.setColor(axisColor);
    mPaintLine.setStrokeWidth(dp2px(context, 10));


    mPaintThumb.setAntiAlias(true);
    mPaintThumb.setStyle(Paint.Style.FILL);
    mPaintThumb.setColor(Color.WHITE);
    if(isShadowLayer){
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
      mPaintThumb.setShadowLayer(10,2,2,Color.GRAY);
    }

    mPaintTestLine.setAntiAlias(true);
    mPaintTestLine.setStyle(Paint.Style.STROKE);
    mPaintTestLine.setColor(Color.RED);

    mPaintText.setAntiAlias(true);
    mPaintText.setStyle(Paint.Style.FILL);
    mPaintText.setColor(Color.BLACK);
    mPaintText.setTextSize(dp2px(context, 10));


    mPaintTriangle.setAntiAlias(true);
    mPaintTriangle.setStyle(Paint.Style.FILL);
    mPaintTriangle.setColor(Color.RED);
    mPaintTriangle.setStrokeWidth(dp2px(context, 3));

  }

  //初始化相关参数
  private void initParam(Context context) {
    thumbRadius = dp2px(context, 10);
    tipBottomMargin = dp2px(context, 5);
    triangleHeight = dp2px(context, 17);
    tipInnerPadding = dp2px(context, 5);
    axisLineHeight = dp2px(context, 7);
    instructTextMarginTop = dp2px(context, 30);
  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvasWidth = canvas.getWidth();
    canvasHeight = canvas.getHeight();

    //根据最长文字宽度来设定坐标轴的水平方向margin
    if (!isInit) {
      // 获取最长文字的矩形
      int textRectWidth = (int) mPaintText.measureText(endText);
      marginHorizontally = (textRectWidth + 2 * tipInnerPadding) / 2;
    }

    //边界线测试用
    if (debugMode) {
      canvas.drawRect(1, 5, canvasWidth - 1, canvasHeight - 5, mPaintTestLine);
    }

    //坐标轴
    axisStart = marginHorizontally;
    axisEnd = canvasWidth - marginHorizontally;

    if (thumbCenterX == -1) {
      thumbCenterX = axisStart;
    }

    //绘制坐标轴
    mPaintLine.setColor(axisColor);
    canvas.drawRect(marginHorizontally, canvasHeight / 2 - axisLineHeight / 2,
        canvasWidth - marginHorizontally, canvasHeight / 2 + axisLineHeight / 2, mPaintLine);
    //绘制左右边圆角
    canvas.drawCircle(marginHorizontally, canvasHeight / 2, axisLineHeight / 2, mPaintLine);
    canvas.drawCircle(canvasWidth - marginHorizontally, canvasHeight / 2,
        axisLineHeight / 2, mPaintLine);

    maxProgressLength = canvasWidth - 2 * marginHorizontally;

    // 展示初始 progress
    if (percentage != -1 && !isInit) {
      thumbCenterX = (int) (maxProgressLength * percentage + marginHorizontally);
    }

    //绘制进度轴
    mPaintLine.setColor(axisProgressColor);
    canvas.drawRect(marginHorizontally, canvasHeight / 2 - axisLineHeight / 2,
        thumbCenterX, canvasHeight / 2 + axisLineHeight / 2, mPaintLine);
    //绘制左圆角
    canvas.drawCircle(marginHorizontally, canvasHeight / 2, axisLineHeight / 2, mPaintLine);


    currentProgressLength = thumbCenterX - marginHorizontally;

    //计算进度并展示文字(代码待优化)
    tipDisplayText = calculateProgress(currentProgressLength, maxProgressLength);

    //滑块
    canvas.drawCircle(thumbCenterX, canvasHeight / 2, thumbRadius, mPaintThumb);


    //绘制三角
    Path pathTriangle = new Path();


    //三角底部中心点坐标
    Point pointTriangleCenter = new Point(thumbCenterX, canvasHeight / 2 - thumbRadius - tipBottomMargin - triangleHeight);


    mPaintTriangle.setColor(Color.RED);

    pathTriangle.moveTo(thumbCenterX, canvasHeight / 2 - thumbRadius - tipBottomMargin);
    pathTriangle.lineTo(thumbCenterX + triangleHeight / 2, canvasHeight / 2 - thumbRadius - tipBottomMargin - triangleHeight);
    pathTriangle.lineTo(thumbCenterX - triangleHeight / 2, canvasHeight / 2 - thumbRadius - tipBottomMargin - triangleHeight);
    pathTriangle.close();

    canvas.drawPath(pathTriangle, mPaintTriangle);


    //绘制文字(文字中心与三角正中央纵向对齐)
    int textRectWidth = (int) mPaintText.measureText(tipDisplayText);


    int textRectHeight = calculateTextBound(mPaintText, tipDisplayText);

    Point pointTextBaseLine = new Point();
    pointTextBaseLine.x = pointTriangleCenter.x - textRectWidth / 2;
    pointTextBaseLine.y = pointTriangleCenter.y - tipInnerPadding;
    mPaintText.setStyle(Paint.Style.FILL);
    canvas.drawText(tipDisplayText, pointTextBaseLine.x, pointTextBaseLine.y, mPaintText);


    //绘制矩形框
    mPaintText.setStyle(Paint.Style.STROKE);
    Rect textOutRect = new Rect(pointTextBaseLine.x - tipInnerPadding,
        pointTextBaseLine.y - textRectHeight - tipInnerPadding,
        pointTextBaseLine.x + textRectWidth + tipInnerPadding,
        pointTextBaseLine.y + tipInnerPadding);

    canvas.drawRect(textOutRect, mPaintText);


    canvas.drawText(startText, tipInnerPadding,
        canvasHeight / 2 + axisLineHeight / 2 + instructTextMarginTop, mPaintText);
    textRectWidth = (int) mPaintText.measureText(endText);
    canvas.drawText(endText, canvasWidth - tipInnerPadding - textRectWidth,
        canvasHeight / 2 + axisLineHeight / 2 + instructTextMarginTop, mPaintText);


    //初始化完成
    if (!isInit) {
      isInit = true;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_UP:
        break;
      case MotionEvent.ACTION_DOWN:
        thumbCenterX = dealRange((int) event.getX());
        invalidate();
        return true;
      case MotionEvent.ACTION_MOVE:
        thumbCenterX = dealRange((int) event.getX());
        invalidate();
        break;
    }
    return super.onTouchEvent(event);
  }

  /**
   * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
   */
  private int dp2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }


  //处理边界
  public int dealRange(int touchX) {
    int resultX = touchX;
    if (touchX < axisStart) {
      resultX = axisStart;
    } else if (touchX > axisEnd) {
      resultX = axisEnd;
    }
    return resultX;
  }

  //计算进度
  //@return 用于展示的display text
  public String calculateProgress(float currLength, float maxLength) {
    float fProgress = currLength / maxLength;
    BigDecimal b = new BigDecimal(fProgress);
    float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    int currentProgress = (int) (f1 * 100);
    String displayText = "";
    if (currentProgress != progress) {
      progress = currentProgress;
      if (listener != null) {
        displayText = listener.onProgressChanged(progress);
      }
    } else {
      displayText = tipDisplayText;
    }
    return displayText;
    //Log.d(TAG, "progress is :" + progress);
  }

  //设置进度监听
  public void setOnProgressChangeListener(OnProgressChangeListener listener) {
    this.listener = listener;
  }


  interface OnProgressChangeListener {
    String onProgressChanged(int progress);
  }


  public void setProgress(int progress) {
    if (progress < 0 || progress > 100) {
      Log.e(TAG, "setProgress is over range!");
      return;
    }

    percentage = progress / 100.0f;

    Log.d(TAG, "percentage is " + percentage);

  }


  public int calculateTextBound(Paint paint, String text) {
    if (TextUtils.isEmpty(text)) {
      Log.e(TAG, "calculateTextBound input illegal !");
      return 0;
    }
    Rect rect = new Rect();
    paint.getTextBounds(text, 0, text.length(), rect);
    return rect.height();
  }


}
