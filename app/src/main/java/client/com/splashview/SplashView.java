package client.com.splashview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class SplashView extends View {
    //旋转圆的画笔
    private Paint mPaint;
    //扩散员的画笔
    private Paint mHolePaint;
    //属性动画
    private ValueAnimator mValueAnimator;
    //背景色
    private int mBackgroundColor= Color.WHITE;
    private int[] mCircleColors;

    //表示旋转圆的中心坐标

    private float mCenterX;
    private float mCenterY;

    //表示斜对角线长度的一半，扩散圆最大半径
    private float mDistance;

    //6个小球的半径
    private float mCircleRadius=18;
    //旋转大圆的半径
    private float mRotateRadius=90;

    //当前大圆的旋转角度
    private float mCurrentRateAngle=0f;
    //当前大圆的半径
    private float mCurrentRoteRadius=mRotateRadius;
    //扩散圆的半径
    private float mCurrentHoleRadius=0f;
    //表示旋转动画的时长
    private int mRotateDuration=1200;



    public SplashView(Context context) {
        this(context,null);
    }

    public SplashView(Context context,  AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SplashView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);

        mHolePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mHolePaint.setStyle(Paint.Style.STROKE);
        mHolePaint.setColor(mBackgroundColor);

        mCircleColors=context.getResources().getIntArray(R.array.splash_circle_colors);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX=w*1f/2;
        mCenterY=h*1f/2;
        mDistance=(float)(Math.hypot(w,h)/2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mState==null){
            mState=new RotateState();
        }
        mState.drawState(canvas);
    }
    private SplashState mState;
    private abstract class SplashState{
        abstract void drawState(Canvas canvas);
    }
    //1.旋转
    private class RotateState extends SplashState{
        private RotateState(){
            mValueAnimator=ValueAnimator.ofFloat(0, (float) (Math.PI*2));//旋转 一周
            mValueAnimator.setRepeatCount(2);//执行两变
            mValueAnimator.setDuration(1200);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            //监听动画的执行过程
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //当前的旋转角度
                    mCurrentRateAngle= (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            //监听动画执行的状态
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mState=new MerginState();
                }
            });
            mValueAnimator.start();
        }
        @Override
        void drawState(Canvas canvas) {
            //绘制背景
            drawBackground(canvas);
            //绘制6个小球
            drawCircle(canvas);
        }


    }
    private void drawCircle(Canvas canvas) {
        //获取两个小球之间的角度
        float rotateAngle= (float) (Math.PI*2/mCircleColors.length);

        for (int i = 0; i < mCircleColors.length; i++) {
            //x=r*cos(a)+centx;
            //y=r*sin(a)+centy;
            float angle=i*rotateAngle+mCurrentRateAngle;
            float cx= (float) (Math.cos(angle)*mCurrentRoteRadius+mCenterX);
            float cy= (float) (Math.sin(angle)*mCurrentRoteRadius+mCenterY);
            mPaint.setColor(mCircleColors[i]);
            canvas.drawCircle(cx,cy,mCircleRadius,mPaint);
        }
    }
    private void drawBackground(Canvas canvas){
        if(mCurrentHoleRadius>0){//表示进行到第三种动画状态了
            //绘制一个空心圆
            float strokeWith=mDistance-mCurrentHoleRadius;
            float radius=strokeWith/2+mCurrentHoleRadius;
            mHolePaint.setStrokeWidth(strokeWith);
            canvas.drawCircle(mCenterX,mCenterY,radius,mHolePaint);
        }else {
            canvas.drawColor(mBackgroundColor);
        }

    }

    //2.扩散 聚合
    private class  MerginState extends SplashState{
        private MerginState (){
            mValueAnimator=ValueAnimator.ofFloat(mCircleRadius,mRotateRadius);
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new OvershootInterpolator(10f));
            //监听动画的执行过程
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //当前的旋转角度
                    mCurrentRoteRadius= (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            //监听动画执行的状态
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mState=new ExpandState();

                }
            });
            mValueAnimator.reverse();
        }
        @Override
        void drawState(Canvas canvas) {
            //绘制背景
            drawBackground(canvas);
            drawCircle(canvas);
        }
    }

    //3.水波纹
    private class ExpandState extends SplashState{
        private ExpandState(){
            mValueAnimator=ValueAnimator.ofFloat(mCircleRadius,mDistance);
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            //监听动画的执行过程
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //当前的旋转角度
                    mCurrentHoleRadius= (float) animation.getAnimatedValue();
                    invalidate();
                }
            });

            mValueAnimator.start();
        }
        @Override
        void drawState(Canvas canvas) {
            drawBackground(canvas);
        }
    }
}
