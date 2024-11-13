package com.ads.control.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ads.control.databinding.ActivityMessageBinding;
import com.ads.control.util.AppUtil;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        listenerInitEvent();
    }

    private void listenerInitEvent(){
        AppUtil.messageInit.observe(this, strings -> {
            binding.txtMessage.setText(strings);
            binding.pbLoading.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}