package com.searchpath.empathy.elastic.util.impl;

import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

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


    public String loadIMDBData() {
        BulkRequest bulk = new BulkRequest();

        var reader = readFile(this.getClass().getClassLoader().getResourceAsStream("data.tsv"));
        var films = readFilmsData(reader);

        System.out.println(films.findFirst().get());

        return null;
    }

    private Stream<Film> readFilmsData(BufferedReader reader) {
        return reader.lines().skip(1).parallel().map(line -> {
            var data = line.split("\t");
            return new Film(data[0], data[2], data[8].split(","), data[5], data[6].equals("\\N") ? null : data[6]);
        });
    }

    private BufferedReader readFile(InputStream dataPath) {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(dataPath, StandardCharsets.UTF_8));
        return reader;
    }

}
