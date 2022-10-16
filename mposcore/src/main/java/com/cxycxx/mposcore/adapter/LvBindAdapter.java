package com.cxycxx.mposcore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


import com.cxycxx.mposcore.custom.OnRvItemClickListener;

import java.util.ArrayList;
import java.util.List;


public class LvBindAdapter<T> extends BaseAdapter {

    private Context context;
    private List<T> list = new ArrayList<>();
    private int layoutId; // 单布局
    private int variableId, clickVariableId;
    private OnRvItemClickListener listener;

    public LvBindAdapter(Context context, List<T> list, int layoutId, int variableId, int clickVariableId) {
        this.context = context;
        if (list != null) this.list = list;
        this.layoutId = layoutId;
        this.variableId = variableId;
        this.clickVariableId = clickVariableId;
    }

    public LvBindAdapter(Context context, int layoutId, int variableId, int clickVariableId) {
        this(context, null, layoutId, variableId, clickVariableId);
    }

    /**
     * 获取数据列表
     *
     * @return
     */
    public List<T> getItemList() {
        return list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 添加监听器
     *
     * @param listener
     */
    public void setOnRvItemClickListener(OnRvItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewDataBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, parent, false);
        } else {
            binding = DataBindingUtil.getBinding(convertView);
        }
        binding.setVariable(variableId, getItem(position));
        if (listener != null) binding.setVariable(clickVariableId, listener);
        return binding.getRoot();
    }
}
