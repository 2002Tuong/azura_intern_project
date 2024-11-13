package com.emopass.antitheftalarm.model;

public class LanguageModel {
    private String languageCode;
    private Integer languageName;
    private Integer resIcon = 0;
    private Boolean isChoose = false;

    public LanguageModel(String languageCode, Integer languageName, Integer resIcon, Boolean isChoose) {
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.resIcon = resIcon;
        this.isChoose = isChoose;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Integer getLanguageName() {
        return languageName;
    }

    public void setLanguageName(Integer languageName) {
        this.languageName = languageName;
    }

    public Integer getResIcon() {
        return resIcon;
    }

    public Boolean getChoose() {
        return isChoose;
    }

    public void setChoose(Boolean choose) {
        isChoose = choose;
    }
}
