package com.ads.control.billing;

public class PurchaseItem {
    private String itemId;
    private String trialId;
    private int type;

    public PurchaseItem(String itemId, int type) {
        this.itemId = itemId;
        this.type = type;
    }

    public PurchaseItem(String itemId, String trialId, int type) {
        this.itemId = itemId;
        this.trialId = trialId;
        this.type = type;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTrialId() {
        return trialId;
    }

    public void setTrialId(String trialId) {
        this.trialId = trialId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
