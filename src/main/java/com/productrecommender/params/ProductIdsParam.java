package com.productrecommender.params;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jersey.params.AbstractParam;

import java.util.ArrayList;
public class ProductIdsParam extends AbstractParam<ArrayList<String>> {

    ObjectMapper mapper = new ObjectMapper();

    protected ProductIdsParam(String input) {
        super(input);
    }

    @Override
    protected ArrayList<String> parse(String s) throws Exception {
        return mapper.readValue(s, ArrayList.class);
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Nope";
        }
    }

    @Override
    protected String errorMessage(String input, Exception e) {
        return '\"' + input + "\" is not a list of strings.";
    }
}
