package com.emopass.antitheftalarm.dialog;

import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.DialogSelectLanguageBinding;
import com.emopass.antitheftalarm.utils.SystemUtil;

import java.util.HashMap;

public class DialogSelectLanguage extends BaseDialog<DialogSelectLanguageBinding, DialogSelectLanguage.ExtendBuilder> {

    private int index;
    private ArrayAdapter mAdapter;

    public DialogSelectLanguage(DialogSelectLanguage.ExtendBuilder builder) {
        super(builder);
    }

    @Override
    protected DialogSelectLanguageBinding getViewBinding() {
        return DialogSelectLanguageBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected void initView() {
        super.initView();
        String code = SystemUtil.getPreLanguage(getContext());
        for (int i = 0; i < SystemUtil.lstCodeLanguage.length; i++) {
            if (code.equals(SystemUtil.lstCodeLanguage[i])) {
                index = i;
                break;
            }
        }
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice, SystemUtil.lstLanguage) {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                CheckedTextView checkedTxtView = (CheckedTextView) super.getView(position, convertView, parent);
                checkedTxtView.setTextColor(getContext().getResources().getColor(R.color.color_333333));
                checkedTxtView.setCheckMarkTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.black)));
                return checkedTxtView;
            }
        };
        binding.rcvData.setAdapter(mAdapter);
        binding.rcvData.setDividerHeight(0);
        binding.rcvData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        binding.rcvData.setItemChecked(index, true);
        binding.rcvData.setOnItemClickListener((arg0, arg1, position1, id) -> {
            index = position1;
        });
    }

    @Override
    protected TextView getPositiveButton() {
        return binding.tvOk;
    }

    @Override
    protected TextView getNegativeButton() {
        return binding.tvCancel;
    }

    @Override
    protected void handleClickPositiveButton(HashMap<String, Object> datas) {
        super.handleClickPositiveButton(datas);
        SystemUtil.setLocale(getContext(), SystemUtil.lstCodeLanguage[index]);
    }

    @Override
    protected void initControl() {

    }

    public static class ExtendBuilder extends BuilderDialog {

        public interface ChangeLanguageListener {
            void onChanged();
        }

        @Override
        public BaseDialog build() {
            return new DialogSelectLanguage(this);
        }

    }
}
