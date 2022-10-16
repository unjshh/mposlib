package com.cxycxx.mposcore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.cxycxx.mposcore.custom.OnRvItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class RvBindAdapter<T> extends RecyclerView.Adapter<RvBindAdapter.ViewHolder> {
    private Context context;
    private List<T> list = new ArrayList<>();
    private int layoutId;//单布局
    private int variableId, clickVariableId;
    private OnRvItemClickListener listener;

    public RvBindAdapter(Context context, List<T> list, int layoutId, int variableId, int clickVariableId) {
        this.context = context;
        if (list != null) this.list = list;
        this.layoutId = layoutId;
        this.variableId = variableId;
        this.clickVariableId = clickVariableId;
    }

    public RvBindAdapter(Context context, int layoutId, int variableId, int clickVariableId) {
        this(context, null, layoutId, variableId, clickVariableId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RvBindAdapter.ViewHolder viewHolder, int i) {
        viewHolder.getBinding().setVariable(variableId, getItem(i));
        if (listener != null) viewHolder.getBinding().setVariable(clickVariableId, listener);
    }

    /**
     * 获取数据列表
     *
     * @return
     */
    public List<T> getItemList() {
        return list;
    }

    /**
     * 获取元素
     *
     * @param position 索引
     * @return
     */
    public T getItem(int position) {
        if (list == null || position < 0 || getItemCount() <= position) return null;
        return list.get(position);
    }

    /**
     * 元素索引
     *
     * @param item
     * @return
     */
    public int indexItem(T item) {
        for (int i = 0, l = getItemCount(); i < l; i++) {
            if (item == getItem(i)) return i;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public void setOnRvItemClickListener(OnRvItemClickListener listener) {
        this.listener = listener;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding mBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public ViewDataBinding getBinding() {
            return mBinding;
        }
    }

}
