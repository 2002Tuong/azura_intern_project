package com.emopass.antitheftalarm.model;

public class OnBoardingModel {
    private String label;
    private Integer imageRes;

    public OnBoardingModel(String label, Integer imageRes) {
        this.label = label;
        this.imageRes = imageRes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getImageRes() {
        return imageRes;
    }

    public void setImageRes(Integer imageRes) {
        this.imageRes = imageRes;
    }
}
