package com.example.garbageclassification.model;

import android.graphics.Bitmap;

public interface IModel {
    String predict(final Bitmap bitmap);
    void close();
}
