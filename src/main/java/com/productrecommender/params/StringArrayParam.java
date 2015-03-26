package com.productrecommender.params;

import io.dropwizard.jersey.params.AbstractParam;

import java.util.ArrayList;
import java.util.Collections;

public class StringArrayParam extends AbstractParam<ArrayList<String>> {
    public StringArrayParam(String input) {
        super(input);
    }

    @Override
    protected ArrayList<String> parse(String input) throws Exception {
        ArrayList<String> output = new ArrayList<>();
        Collections.addAll(output, input.split(","));
        return output;
    }
}
