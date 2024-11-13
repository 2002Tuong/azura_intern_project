package com.emopass.antitheftalarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import com.emopass.antitheftalarm.databinding.ItemLanguageViewBinding;
import com.emopass.antitheftalarm.model.LanguageModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    private final Context context;
    private final List<LanguageModel> languageModelList = new ArrayList<>();
    private OnLanguageChange onLanguageChangedCallback;

    public LanguageAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<LanguageModel> list) {
        languageModelList.clear();
        languageModelList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLanguageViewBinding binding = ItemLanguageViewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LanguageModel language = languageModelList.get(position);
        holder.getBinding().imgLanguage.setImageDrawable(
                AppCompatResources.getDrawable(
                        context,
                        language.getResIcon()
                )
        );
        holder.getBinding().icChoose.setSelected(language.getChoose());
        holder.getBinding().ctlItemLanguage.setSelected(language.getChoose());
        holder.getBinding().txtLanguage.setText(context.getString(language.getLanguageName()));
        holder.itemView.setOnClickListener(view -> {
            updateView(language);
            onLanguageChangedCallback.onChange(itemSelected().getLanguageCode());
        });
    }

    @Override
    public int getItemCount() {
        return languageModelList.size();
    }

    private void updateView(LanguageModel languageModel) {
        for (LanguageModel model: languageModelList) {
            model.setChoose(Objects.equals(model.getLanguageCode(), languageModel.getLanguageCode()));
        }
        notifyDataSetChanged();
    }

    public LanguageModel itemSelected() {
        LanguageModel itemSelected = null;
        for (LanguageModel item: languageModelList) {
            if(item.getChoose()) {
                itemSelected = item;
                break;
            }
        }
        return  itemSelected;
    }

    public void setOnLanguageChange(OnLanguageChange callBack) {
        onLanguageChangedCallback = callBack;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemLanguageViewBinding binding;
        public ViewHolder(ItemLanguageViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemLanguageViewBinding getBinding() {
            return  binding;
        }
    }

    public interface OnLanguageChange {
        void onChange(String language);
    }
}
