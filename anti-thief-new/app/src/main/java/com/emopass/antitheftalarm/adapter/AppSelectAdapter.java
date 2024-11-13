package com.emopass.antitheftalarm.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.ItemAppSelectBinding;
import com.emopass.antitheftalarm.model.TaskInfo;

import java.util.List;

public class AppSelectAdapter extends BaseRecyclerAdapter<TaskInfo, AppSelectAdapter.ViewHolder> {

    private ItemClickListener itemClickListener;
    private PackageManager packageManager;

    public AppSelectAdapter(Context context, List<TaskInfo> list) {
        super(context, list);
        this.packageManager = mContext.getPackageManager();
    }

    public interface ItemClickListener {
        void OnClickItem(int position);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(AppSelectAdapter.ViewHolder holder, int position) {
        holder.binData(list.get(position));
    }

    @Override
    public AppSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = layoutInflater.inflate(R.layout.item_app_select, parent, false);
        return new ViewHolder(mView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ItemAppSelectBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemAppSelectBinding.bind(itemView);
        }

        public void binData(TaskInfo mTaskInfo) {
            if (mTaskInfo != null) {
                if (!TextUtils.isEmpty(mTaskInfo.getTitle()))
                    binding.tvAppname.setText(mTaskInfo.getTitle());
//                Glide.with(binding.imIcon)
//                        .load(mTaskInfo.getAppinfo().loadIcon(packageManager))
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .error(R.drawable.ic_app_default)
//                        .skipMemoryCache(true)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(binding.imIcon);
                binding.imIcon.setImageDrawable(mTaskInfo.getAppinfo().loadIcon(packageManager));
                binding.swSelect.setChecked(mTaskInfo.isChceked());
                binding.swSelect.setOnClickListener(v -> {
                    if (mTaskInfo.isClickEnable())
                        callClick(mTaskInfo);
                    else
                        binding.swSelect.setChecked(mTaskInfo.isChceked());
                });
            }
        }

        public void callClick(TaskInfo mTaskInfo) {
            mTaskInfo.setChceked(!mTaskInfo.isChceked());
            if (itemClickListener != null)
                itemClickListener.OnClickItem(getAdapterPosition());
        }
    }
}
