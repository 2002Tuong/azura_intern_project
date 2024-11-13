package com.emopass.antitheftalarm.widget;

public interface CheckPasswordCodeListener {
    void onCheck(State state, String passInput);

    enum State {
        SUCCESS, FAILED
    }
}
