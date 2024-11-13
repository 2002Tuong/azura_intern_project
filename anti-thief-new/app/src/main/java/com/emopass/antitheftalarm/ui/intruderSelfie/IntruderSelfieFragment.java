package com.emopass.antitheftalarm.ui.intruderSelfie;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.adapter.SelfieAdapter;
import com.emopass.antitheftalarm.databinding.FragmentIntruderSelfieBinding;
import com.emopass.antitheftalarm.dialog.DialogSelectItem;
import com.emopass.antitheftalarm.ui.BaseFragment;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.SortUtil;
import com.emopass.antitheftalarm.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IntruderSelfieFragment extends BaseFragment<FragmentIntruderSelfieBinding> {

    private List<File> lstData = new ArrayList<>();
    private SelfieAdapter selfieAdapter;

    @Override
    protected void initView() {
        selfieAdapter = new SelfieAdapter(getActivity(), lstData);
        binding.rcvData.setAdapter(selfieAdapter);
        File[] lstFile = FileUtil.getAllImageFromInternal(getContext());
        if (lstFile == null || lstFile.length == 0) {
            return;
        }
        fillData(SortUtil.sortTimeFile(lstFile, true));
    }

    private void fillData(List<File> datas) {
        if (datas == null || datas.size() == 0) {
            binding.tvNodata.setVisibility(View.VISIBLE);
            binding.rcvData.setVisibility(View.GONE);
        } else {
            binding.tvNodata.setVisibility(View.GONE);
            binding.rcvData.setVisibility(View.VISIBLE);
        }
        lstData.clear();
        lstData.addAll(datas);
        selfieAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initControl() {

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater mMenuInflater) {
        mMenuInflater.inflate(R.menu.menu_selfie, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_incorrect_password:
                new DialogSelectItem.ExtendBuilder()
                        .setLstData(Config.lstIncorrectPasswordEntries)
                        .setIdDefault(PreferencesHelper.getInt(PreferencesHelper.INTRUDER_SELFIE_ENTRIES
                                , Config.DEFAULT_INTRUDER_SELFIE))
                        .setTitle(getString(R.string.incorrect_password_entries))
                        .onSetPositiveButton(getString(R.string.save), (baseDialog, datas) -> {
                            int idSelected = (int) datas.get(DialogSelectItem.ITEM_SAVE);
                            PreferencesHelper.putInt(PreferencesHelper.INTRUDER_SELFIE_ENTRIES, idSelected);
                            baseDialog.dismiss();
                        })
                        .build()
                        .show(getChildFragmentManager(), DialogSelectItem.class.getName());
                return true;
            case R.id.action_empty:
                deleteImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteImage() {
        for (File file : lstData) {
            file.delete();
        }
        lstData.clear();
        selfieAdapter.notifyDataSetChanged();
        binding.tvNodata.setVisibility(View.VISIBLE);
        binding.rcvData.setVisibility(View.GONE);
    }

    @Override
    protected FragmentIntruderSelfieBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentIntruderSelfieBinding.inflate(inflater, container, false);
    }

    @Override
    protected int getTitleFragment() {
        return R.string.intruder_selfie;
    }
}
