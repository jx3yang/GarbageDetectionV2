package com.example.garbageclassification.model;

public class ModelFactory {
    public IModel createModel(ModelType modelType) {
        return new TfModel();
    }
}
