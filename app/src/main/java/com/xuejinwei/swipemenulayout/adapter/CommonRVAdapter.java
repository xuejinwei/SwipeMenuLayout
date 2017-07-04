package com.xuejinwei.swipemenulayout.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuejinwei on 2017/7/4.
 * RecyclerView通用的adapter
 * 1.数据操作添加数据 获取数据  删除数据……
 * 2.增加header 和 footer，addHeaderView,addFooterView,也可以通过remove移除header和footer
 */
public abstract class CommonRVAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {
    private static final String TAG = "CommonRVAdapter";

//    public static final int VIEW_TYPE_HEADER = 1024;
//    public static final int VIEW_TYPE_FOOTER = 1025;

//    protected View headerView;
//    protected View footerView;

    private CommonViewHolder mHeaderHolder;
    private CommonViewHolder mFooterHolder;

    protected Context                  mContext;
    protected int                      mLayoutId;
    protected List<T>                  mDatas;
    protected LayoutInflater           mInflater;
    private   OnGItemClickListener     onGItemClickListener;
    private   OnGItemLongClickListener onGItemLongClickListener;

    /**
     * 创建一个CommonRVAdapter
     * 注：传入的数据集合，执行的是 mDatas = datas，即传递的是引用，不是新建一个List添加所有。方便外面对adapter的数据进行操作
     * 外部执行{@link #getAll()}获取的和此处传入的datas是同一个引用，所有外部没必要执行{@link #getAll()}获取数据，直接使用datas就是adapter的数据
     *
     * @param context  context
     * @param layoutId adapter的布局文件，
     * @param datas    List数据集，传递的是引用
     */
    public CommonRVAdapter(Context context, @LayoutRes int layoutId, List<T> datas) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutId = layoutId;
        mDatas = datas;
    }

    public CommonRVAdapter(Context context, @LayoutRes int layoutId) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutId = layoutId;
        mDatas = new ArrayList<>();
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: " + viewType);

        if (viewType == getHeaderType()) {
            return mHeaderHolder;
        } else if (viewType == getFooterType()) {
            return mFooterHolder;
        } else {
            return CommonViewHolder.createViewHolder(mContext, parent, mLayoutId);
        }
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        if (holder.getItemViewType() == getHeaderType()) {
            return;
        } else if (holder.getItemViewType() == getFooterType()) {
            return;
        } else {
            // 这里如果有header的话，就减去header的个数
            convert(holder, mDatas.get(position - getHeaderExtraViewCount()));
            convert(holder, mDatas.get(position - getHeaderExtraViewCount()), position);
            setListener(holder, position - getHeaderExtraViewCount());
        }
    }

    public abstract void convert(CommonViewHolder gViewHolder, T t);

    public void convert(CommonViewHolder gViewHolder, T t, int position) {

    }

    ;

    @Override
    public int getItemViewType(int position) {
        if (mHeaderHolder != null && position == 0) {
            return getHeaderType();
        } else if (mFooterHolder != null && position == mDatas.size() + getHeaderExtraViewCount()) {
            return getFooterType();
        } else {
            return super.getItemViewType(position);
        }
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == getHeaderType() || getItemViewType(position) == getFooterType()
                            ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    private int getHeaderType() {
        return mHeaderHolder == null ? -1 : mHeaderHolder.hashCode();
    }

    private int getFooterType() {
        return mFooterHolder == null ? -1 : mFooterHolder.hashCode();
    }

    @Override
    public void onViewAttachedToWindow(CommonViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        View itemView = holder.itemView;
        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        if (lp == null) {
            return;
        }
        if (getItemViewType(holder.getAdapterPosition()) == getHeaderType() || getItemViewType(holder.getAdapterPosition()) == getFooterType()) {

            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    /**
     * 设置item点击
     */
    private void setListener(final CommonViewHolder viewHolder, final int position) {
        if (onGItemClickListener != null) {
            viewHolder.getConvertView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onGItemClickListener != null) {
                        onGItemClickListener.onItemClick(mDatas.get(position), position);
                    }
                }
            });
        }
        if (onGItemLongClickListener != null) {
            viewHolder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onGItemLongClickListener != null) {
                        return onGItemLongClickListener.onItemLongClick(mDatas.get(position), position);
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 添加HeaderView
     *
     * @param headerView 顶部View对象
     */
    public void addHeaderView(View headerView) {
        if (headerView == null) {
            Log.w(TAG, "add the header view is null");
            return;
        }
        this.mHeaderHolder = CommonViewHolder.createViewHolder(mContext, headerView);
        notifyDataSetChanged();
    }

    /**
     * 移除HeaderView
     */
    public void removeHeaderView() {
        if (mHeaderHolder != null) {
            mHeaderHolder = null;
            notifyDataSetChanged();
        }
    }

    /**
     * 添加FooterView
     *
     * @param footerView View对象
     */
    public void addFooterView(View footerView) {
        if (footerView == null) {
            Log.w(TAG, "add the footer view is null");
            return;
        }
        this.mFooterHolder = CommonViewHolder.createViewHolder(mContext, footerView);
        notifyDataSetChanged();
    }

    /**
     * 移除FooterView
     */
    public void removeFooterView() {
        if (mFooterHolder != null) {
            mFooterHolder = null;
            notifyDataSetChanged();
        }
    }

    /**
     * 获取附加View的数量,包括HeaderView和FooterView
     *
     * @return 数量
     */
    public int getExtraViewCount() {
        int extraViewCount = 0;
        if (mHeaderHolder != null) {
            extraViewCount++;
        }
        if (mFooterHolder != null) {
            extraViewCount++;
        }
        return extraViewCount;
    }

    /**
     * 获取顶部附加View数量,即HeaderView数量
     *
     * @return 数量
     */
    public int getHeaderExtraViewCount() {
        return mHeaderHolder == null ? 0 : 1;
    }

    /**
     * 获取底部附加View数量,即FooterView数量
     *
     * @return 数量, 0或1
     */
    public int getFooterExtraViewCount() {
        return mFooterHolder == null ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        if (mDatas == null) {
            return getExtraViewCount();
        }
        return mDatas.size() + getExtraViewCount();
    }


    /****************数据操作相关*************/

    /**
     * 添加数据
     */
    public void addAll(List<T> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    /**
     * 获得数据
     */
    public List<T> getAll() {
        return mDatas;
    }

    /**
     * 增加一行数据,末尾
     */
    public void addItem(T data) {
        mDatas.add(data);
        notifyItemInserted(mDatas.size());
    }

    /**
     * 增加一行数据，在position位置
     */
    public void addItem(int position, T data) {
        mDatas.add(position, data);
        notifyItemInserted(position);
        if (position != mDatas.size()) {
            // 执行notifyItemInserted，不重新onBindViewHolder，执行如下代码强制重新刷新position后面的的viewholder，否则position等不对
            notifyItemRangeChanged(position + getHeaderExtraViewCount(), mDatas.size() - position);
        }
    }

    /**
     * 删除一行数据
     */
    public void removeItem(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);
        if (position != mDatas.size()) {
            // 执行notifyItemRemoved，不重新onBindViewHolder，执行如下代码强制重新刷新position后面的viewholder，否则position等不对
            notifyItemRangeChanged(position + getHeaderExtraViewCount(), mDatas.size() - position);
        }
    }

    /**
     * 清空数据
     */
    public void clear() {
        mDatas.clear();
        notifyDataSetChanged();
    }
    /****************数据操作相关end*************/


    /**
     * 设置RV的item点击监听
     *
     * @param onGItemClickListener 传入{@link OnGItemClickListener}
     */
    public void setOnGItemClickListener(OnGItemClickListener<T> onGItemClickListener) {
        this.onGItemClickListener = onGItemClickListener;
    }

    /**
     * 设置RV的item长按监听事件
     *
     * @param onGItemLongClickListener 传入 {@link OnGItemLongClickListener}
     */
    public void setOnGItemLongClickListener(OnGItemLongClickListener<T> onGItemLongClickListener) {
        this.onGItemLongClickListener = onGItemLongClickListener;

    }

    public interface OnGItemClickListener<T> {
        void onItemClick(T data, int position);
    }

    public interface OnGItemLongClickListener<T> {
        boolean onItemLongClick(T data, int position);
    }
}
