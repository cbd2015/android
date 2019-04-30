package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 *  1、手动画笔绘制的是任意尺寸的图像；
 *  2、通过尺寸换算，图像Bitmap数据转换处理；
 *  3、标准输出尺寸——》神经网络模型中；
 *
 */
public class ClipImage extends View{

    private Context mContext;

    /**
     * 笔画X坐标起点
     */
    private float mX;

    /**
     * 笔画Y坐标起点
     */
    private float mY;
    private PointF minPoint=new PointF();
    private PointF maxPoint=new PointF();

    /**
     * 手写画笔
     */
    private final Paint mGesturePaint = new Paint();
    /**
     * 路径
     */
    private final Path mPath = new Path();
    /**
     * 画笔宽度 px；
     */
    private int mPaintWidth =10;
    /**
     * 前景色
     */
    private int mPenColor = 0xffff94a6;
    /**
     * 背景色（指最终签名结果文件的背景颜色，默认为透明色）
     */


    /**
     * 是否已经签名
     */
    private boolean isTouched = false;
    private int mBackColor=Color.TRANSPARENT;
    public interface OnDrawListener{
        void onDrawFinished(PointF min,PointF max);
    }
    private OnDrawListener onDrawListener;

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    public ClipImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.dispatchTouchEvent(event);
    }

    public void init(Context context) {
        this.mContext = context;
        //设置抗锯齿
        mGesturePaint.setAntiAlias(true);
        //设置签名笔画样式
        mGesturePaint.setStyle(Style.STROKE);
        //设置笔画宽度
        mGesturePaint.setStrokeWidth(mPaintWidth);
        //设置签名颜色
        mGesturePaint.setColor(mPenColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//		画此次笔画之前的签名
//      canvas.drawBitmap(cachebBitmap, 0, 0, mGesturePaint);
        // 通过画布绘制多点形成的图形
        canvas.drawPath(mPath, mGesturePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //创建跟view一样大的bitmap，用来保存签名
        isTouched=false;
    }

    /**
     * 触发绘制事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action=event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onUp(event);
                break;
            default:
                break;
        }
        // 更新绘制
        invalidate();

        return true;
    }
    public void clear(){
        mPath.reset();
        // 画布失效，重新绘制
        invalidate();
    }

    /**
     * 画笔起始点，笔尖落下位置
     * @param event
     */
    private void onDown(MotionEvent event){
        mX=event.getX();
        mY=event.getY();
        mPath.reset();
        mPath.moveTo(mX, mY);
        minPoint.set(mX, mY);
        maxPoint.set(mX, mY);
    }

    /**
     * 滑动笔尖，开始绘制
     * @param event
     */
    private void onMove(MotionEvent event){
        final float x = event.getX();
        final float y = event.getY();
        minPoint.set(x<minPoint.x?x:minPoint.x, y<minPoint.y?y:minPoint.y);
        maxPoint.set(x>maxPoint.x?x:maxPoint.x, y>maxPoint.y?y:maxPoint.y);
        final float previousX = mX;
        final float previousY = mY;
        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);
        /**
         *   手动绘制图形，图像上的几个点能够绘制出平滑的曲线
         */
        // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            // 设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;
            // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);
            // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
        }

    }

    /**
     * 画笔提起，停止绘制
     * @param event
     */
    private void onUp(MotionEvent event){
        mPath.close();
        if(onDrawListener!=null)
            onDrawListener.onDrawFinished(minPoint, maxPoint);
    }

}
