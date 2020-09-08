package com.example.garbageclassification.model;

import android.app.Activity;

import java.io.IOException;

public class ModelFactory {
    public static IModel createModel(Activity activity) throws IOException {
        return new TfModel(activity, 1);
    }
}
