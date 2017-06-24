package com.xuejinwei.libs;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by xuejinwei on 2017/6/24.
 * Email:xuejinwei@outlook.com
 * 自己写的侧滑带menu 的viewgroup
 */

public class SwipMenuLayout extends ViewGroup {

    private static String TAG = "SwipMenuLayout";

    /**
     * 用于完成滚动操作的实例
     */
    private Scroller mScroller;

    /**
     * 判定为拖动的最小移动像素数
     */
    private int mTouchSlop;

    /**
     * 手机按下时的屏幕坐标
     */
    private float mXDown;

    /**
     * 手move时所处的屏幕坐标
     */
    private float mXMove;

    /**
     * 上次触发ACTION_MOVE事件时的屏幕坐标
     */
    private float mXLastMove;

    /**
     * 界面可滚动的左边界
     */
    private int leftBorder;

    /**
     * 界面可滚动的右边界
     */
    private int rightBorder;

    public SwipMenuLayout(Context context) {
        this(context, null);
    }

    public SwipMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(context);

        mTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        int wrapHeigh = 0;
        int wrapWidth = 0;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 为ScrollerLayout中的每一个子控件测量大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            wrapWidth = wrapWidth + childView.getMeasuredWidth();//所以view宽度总和
            wrapHeigh = wrapHeigh > childView.getMeasuredHeight() ? wrapHeigh : childView.getMeasuredHeight();// 所以view 的最高的高度
        }
        int width = measureDimension(wrapWidth, widthMeasureSpec);
        int height = measureDimension(wrapHeigh, heightMeasureSpec);
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            int left = 0;

            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                childView.setClickable(true);
                childView.layout(left, 0, left + childView.getMeasuredWidth(), childView.getMeasuredHeight());
                left = left + childView.getMeasuredWidth();
            }
            // 初始化左右边界值
            leftBorder = getChildAt(0).getLeft();
            rightBorder = getChildAt(getChildCount() - 1).getRight();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 首次按下，记录相关点击的位置
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                mXLastMove = mXMove;
                float diff = Math.abs(mXMove - mXDown);
                // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mXMove = event.getRawX();
                int scrolledX = (int) (mXLastMove - mXMove);
                Log.i(TAG, "getScrollX-----:" + getScrollX() + ";" + "scrolledX-----:" + scrolledX + ";" + "getWidth-----" + getWidth() + "");
                if (getScrollX() + scrolledX < leftBorder) {
                    scrollTo(leftBorder, 0);
                    return true;
                } else if (getScrollX() + getWidth() + scrolledX > rightBorder) {
                    scrollTo(rightBorder - getWidth(), 0);
                    return true;
                }
                scrollBy(scrolledX, 0);
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    public int measureDimension(int defaultSize, int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {// match_parent,100dp
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {// wrap_content
            result = Math.min(defaultSize, specSize);
        } else {// UNSPECIFIED
            result = defaultSize;
        }
        return result;
    }
}

