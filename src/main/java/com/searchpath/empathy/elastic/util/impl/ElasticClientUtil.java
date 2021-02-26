package com.searchpath.empathy.elastic.util.impl;

import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Class containing helper methods to interact with the Elastic Client.
 * This class methods manage the exceptions in order to have a more readable code elsewhere.
 * */
@Singleton
public class ElasticClientUtil implements IElasticUtil {

    @Inject
    ElasticClient client;

    public String getClusterName() throws IOException {

        MainResponse response = null;
        response = client.getClient().info(RequestOptions.DEFAULT);
        return response.getClusterName();
    }

}
