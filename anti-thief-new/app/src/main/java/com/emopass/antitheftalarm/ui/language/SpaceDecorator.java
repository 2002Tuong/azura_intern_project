package com.emopass.antitheftalarm.ui.language;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceDecorator extends RecyclerView.ItemDecoration {
    private final int space;
    public SpaceDecorator(int spaceBetween) {
        space = spaceBetween;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = space;
    }
}
