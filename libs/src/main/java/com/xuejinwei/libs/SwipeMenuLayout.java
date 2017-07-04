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
 * 第一个子Item永远为percent的宽度，即作为主view,其他后面的一次layout布局，为menu
 * <p>
 * 开发过程，详情看tag
 * 01.通过scrollto、scrollby实现侧滑功能
 * 02.通过Scroller实现抬手平滑过度
 * 03.展开情况下，点击content关闭
 */

public class SwipeMenuLayout extends ViewGroup {

    private static String TAG = "SwipMenuLayout";

    private Scroller mScroller;// 用于完成平滑滚动过度的操作的实例

    private int mTouchSlop;// 判定为拖动的最小移动像素数

    private float mXDown;// 手指首次按下时的屏幕坐标
    private float mXMove;// 手move时所处的屏幕坐标
    private float mXLastMove;// 上次触发ACTION_MOVE事件时的屏幕坐标

    private int leftBorder;// 界面滚动区域的左边界
    private int rightBorder;// 界面滚动区域的右边界

    private boolean isUserSwiped;// 根据手指起落点，判断是不是滑动事件
    private boolean isStateExpand;// menu是否处于展开状态

    private static SwipeMenuLayout mViewCache;// 存储当前正在展开的SwipeMenuLayout
    private        boolean         isIntercept;// 是否拦截

    public SwipeMenuLayout(Context context) {
        this(context, null);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
            Log.i(TAG, "rightBorder-----:" + rightBorder);
            Log.i(TAG, "leftBorder-----:" + leftBorder);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mViewCache != null && ev.getX() < getWidth() - getScrollX()) {
                    if (mViewCache != this) {
                        mViewCache.smoothClose();// 如果mViewCache不是自身，则平滑关闭，且 mViewCache 设为null
                    }
                    //只要有一个侧滑菜单处于打开状态， 就不给外层布局上下滑动了
                    getParent().requestDisallowInterceptTouchEvent(true);
                    isIntercept = true;
                }
                // 首次按下，记录相关点击的位置
                mXDown = ev.getRawX();
                mXLastMove = ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 滑动，记录相关滑动的位置
                mXMove = ev.getRawX();
                mXLastMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
                if (diff > mTouchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);// 为了在水平滑动中禁止父类ListView等再竖直滑动
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isIntercept || isStateExpand && ev.getX() < getWidth() - getScrollX()) {// 说明点击在滑动展开状态的content区域，拦截,
                    isIntercept = false;
                    smoothClose();// 平滑关闭
                    return true;// 拦截事件
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
                if (getScrollX() + scrolledX < leftBorder) {//越左界修正
                    quickClose();
                    return true;
                } else if (getScrollX() + getWidth() + scrolledX > rightBorder) {//越右界修正
                    quickExpand();
                    return true;
                }
                Log.i(TAG + "-MOVE", "getScrollX--:" + getScrollX() + ";" + "mXMove--" + mXMove + ";" + "mXLastMove--:" + mXLastMove + ";" + "mXDown" + mXDown);
                scrollBy(scrolledX, 0);
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
                mXMove = event.getRawX();
                // 调用startScroll()进行平滑过渡
                if (mXMove - mXDown < 0) {// 左滑展开
                    smoothExpand();
                } else if (mXMove - mXDown > 0) {// 右滑关闭
                    smoothClose();
                }
                Log.i(TAG + "-UP", "getScrollX--:" + getScrollX() + ";" + "mXMove--" + mXMove + ";" + "mXLastMove--:" + mXLastMove + ";" + "mXDown" + mXDown);
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 平滑的展开
     */
    public void smoothExpand() {
        mViewCache = this;
        mScroller.startScroll(getScrollX(), 0, rightBorder - getWidth() - getScrollX(), 0);
        invalidate();
        isStateExpand = true;
    }

    /**
     * 平滑的关闭
     */
    public void smoothClose() {
        mViewCache = null;
        mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0);
        invalidate();
        isStateExpand = false;
    }

    /**
     * 快速展开
     */
    public void quickExpand() {
        scrollTo(rightBorder - getWidth(), 0);
        isStateExpand = true;
    }

    /**
     * 快速关闭
     */
    public void quickClose() {
        scrollTo(leftBorder, 0);
        isStateExpand = false;
    }

    /**
     * 每次ViewDetach的时候，判断一下 ViewCache是不是自己，如果是自己，关闭侧滑菜单，且ViewCache设置为null，
     * 1 防止内存泄漏(ViewCache是一个静态变量)
     * 2 侧滑删除后自己后，这个View被Recycler回收，复用，下一个进入屏幕的View的状态应该是普通状态，而不是展开状态。
     */
    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.smoothClose();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void computeScroll() {
        // 重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
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

