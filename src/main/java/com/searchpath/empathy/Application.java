package com.searchpath.empathy;

import com.searchpath.empathy.elastic.util.impl.ElasticClientUtil;
import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {
        var util = new ElasticClientUtil();
        util.loadIMDBData();
        //Micronaut.run(Application.class, args);
    }
}
