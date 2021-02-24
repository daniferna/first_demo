package com.searchpath.empathy.elastic;

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
public class ClientElasticUtil implements IElasticUtil {

    @Inject
    ElasticClient client;

    public String getClusterName() {

        MainResponse response = null;
        try {
            response = client.getClient().info(RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.getClusterName();
    }

}
