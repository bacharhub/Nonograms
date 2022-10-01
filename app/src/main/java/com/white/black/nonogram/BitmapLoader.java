package com.white.black.nonogram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

public enum BitmapLoader {

    INSTANCE;

    private final SparseArray<Bitmap> bitmaps;

    BitmapLoader() {
        bitmaps = new SparseArray<>();
    }

    public Bitmap getImage(Context context, int imageId) {
        Bitmap bitmap = bitmaps.get(imageId);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), imageId);
            if (bitmap != null) {
                bitmaps.put(imageId, bitmap);
            }
        }

        return bitmap;
    }
}
