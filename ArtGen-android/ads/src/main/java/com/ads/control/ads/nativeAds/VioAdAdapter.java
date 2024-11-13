package com.ads.control.ads.nativeAds;

import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;

public class VioAdAdapter {
    private AdmobRecyclerAdapter admobRecyclerAdapter;
    private MaxRecyclerAdapter maxRecyclerAdapter;

    public VioAdAdapter(AdmobRecyclerAdapter admobRecyclerAdapter) {
        this.admobRecyclerAdapter = admobRecyclerAdapter;
    }

    public VioAdAdapter(MaxRecyclerAdapter maxRecyclerAdapter) {
        this.maxRecyclerAdapter = maxRecyclerAdapter;
    }

    public RecyclerView.Adapter getAdapter() {
        if (admobRecyclerAdapter != null) return admobRecyclerAdapter;
        return maxRecyclerAdapter;
    }

    public void notifyItemRemoved(int pos) {
        if (maxRecyclerAdapter != null) {
            maxRecyclerAdapter.notifyItemRemoved(pos);
        }
    }

    public int getOriginalPosition(int pos) {
        if (maxRecyclerAdapter != null) {
           return maxRecyclerAdapter.getOriginalPosition(pos);
        }
        if (admobRecyclerAdapter !=null){
           return admobRecyclerAdapter.getOriginalPosition(pos);
        }
        return 0;
    }

    public void loadAds() {
        if (maxRecyclerAdapter != null)
            maxRecyclerAdapter.loadAds();
    }

    public void destroy() {
        if (maxRecyclerAdapter != null)
            maxRecyclerAdapter.destroy();
    }

    public void setCanRecyclable(boolean canRecyclable){
        if(admobRecyclerAdapter != null){
            admobRecyclerAdapter.setCanRecyclable(canRecyclable);
        }
    }

    public void setNativeFullScreen(boolean nativeFullScreen){
        if(admobRecyclerAdapter != null){
            admobRecyclerAdapter.setNativeFullScreen(nativeFullScreen);
        }
    }
}
