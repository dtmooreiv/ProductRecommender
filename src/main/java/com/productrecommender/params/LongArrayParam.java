package com.productrecommender.params;

import io.dropwizard.jersey.params.AbstractParam;

import java.util.ArrayList;

public class LongArrayParam extends AbstractParam<ArrayList<Long>> {
    public LongArrayParam(String input) {
        super(input);
    }

    protected ArrayList<Long> parse(String input) {
        ArrayList<Long> output = new ArrayList<>();
        for (String s: input.split(",")){
            output.add(Long.valueOf(s));
        }
        return output;
    }
}
