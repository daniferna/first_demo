package com.searchpath.empathy.elastic.util.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import com.searchpath.empathy.pojo.QueryResponse;
import com.searchpath.empathy.pojo.TermAggregationPojo;
import com.searchpath.empathy.pojo.TermBucketPojo;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class containing helper methods to interact with the Elastic Client.
 * This class methods manage the exceptions in order to have a more readable code elsewhere.
 */
@Singleton
public class ElasticClientUtil implements IElasticUtil {

    @Inject
    ElasticClient client;

    @Inject
    ObjectMapper objectMapper;


    /**
     * Use the client info method {@link org.elasticsearch.client.RestHighLevelClient#info(RequestOptions)}
     * to get the cluster name.
     */
    @Override
    public String getClusterName() throws IOException {
        MainResponse response;
        response = client.getClient().info(RequestOptions.DEFAULT);
        return response.getClusterName();
    }

    /**
     * This implementation reads the file {@see #readFile(InputStream)} and then, it divides the entry into chunks
     * of 50.000 lines with the help of the Guava's Iterators library help. {@see Iterators#partition(Iterator, int)}
     * <p>
     * Following that, read each chunk and proceeds to create a {@link Film} object which is then deserialized into
     * a Json and added to a {@link BulkRequest}. Next step is take the elastic search client and upload this bulk.
     * <p>
     * This is repeat for every chunk.
     *
     * @throws IOException If the method can't deserialize the film object into JSON
     *                     or an error occur while loading the bulk data through the client.
     */
    @Override
    public String loadIMDBData() throws IOException {
        if (!client.getClient().indices().exists(new GetIndexRequest("imdb"), RequestOptions.DEFAULT))
            createIndex();

        var reader = readFile(this.getClass().getClassLoader().getResourceAsStream("data.tsv"));
        var bulk = new BulkRequest();

        // Split the data stream in chunks of 10.000 lines in a lazy way.
        UnmodifiableIterator<List<String>> linesList = Iterators.partition(reader.lines().skip(1).iterator(), 10000);

        while (linesList.hasNext()) {
            List<String> list = linesList.next();

            for (String line : list) {
                Film film = createFilmFromLine(line);
                bulk.add(new IndexRequest("imdb").id(film.getId())
                        .source(objectMapper.writeValueAsString(film), XContentType.JSON));
            }

            client.getClient().bulk(bulk, RequestOptions.DEFAULT);
            bulk = new BulkRequest();
        }

        reader.close();
        return "Success loading data";
    }

    /**
     * Helper method, creates a {@see Film} POJO from a line extracted from the data source.
     *
     * @param line String containing the info of the film separated by tabs.
     * @return A Film POJO
     */
    private Film createFilmFromLine(String line) {
        var data = line.split("\t");
        var film = new Film(data[0], data[2],
                data[8].equals("\\N") ? null : data[8].split(","),
                data[1],
                data[5].equals("\\N") ? null : data[5] + "-01-01",
                data[6].equals("\\N") ? null : data[6] + "-01-01");
        return film;
    }

    /**
     * Helper method, creates the index in elasticsearch using the IMDB Index json config file.
     * {<a href="file:../../../resources/mappingIMDBIndex.json"}
     *
     * @throws IOException If an error occur while reading the config file.
     */
    private void createIndex() throws IOException {
        CreateIndexRequest create = new CreateIndexRequest("imdb");

        var json = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResourceAsStream("mappingIMDBIndex.json"), "mapping JSON not found");

        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        json.close();

        create.source(map);

        client.getClient().indices().create(create, RequestOptions.DEFAULT);
    }

    /**
     * This implementation use the {@link ElasticClient} API to look for the media.
     * It creates a {@link SearchRequest}, and using a {@link MultiMatchQueryBuilder}, creates the query.
     * <p>
     * Once the response is obtained, is processed by a private helper method {@link #getQueryResponse(SearchRequest)} )}
     * which transform the hits into {@link Film} objects.
     *
     * @throws IOException If the method can't serialize the hit into a Film JSON or an error occur while searching
     *                     the query through the client.
     */
    @Override
    public QueryResponse search(String query) throws IOException {
        var request = new SearchRequest("imdb");

        var queryBuilder = new MultiMatchQueryBuilder(query, "title", "genres", "type", "start_year.getYear");
        queryBuilder.field("title", 3);
        queryBuilder.field("type", 2);
        queryBuilder.type(MultiMatchQueryBuilder.Type.MOST_FIELDS);
        request.source(getSearchSourceBuilder(queryBuilder));

        return getQueryResponse(request);
    }

    /**
     * This implementation use the {@link ElasticClient} API to look for the media.
     * It creates a {@link SearchRequest}, and using a {@link MatchQueryBuilder}, creates the query.
     * <p>
     * Once the response is obtained, is processed by a private helper method {@link #getQueryResponse(SearchRequest)} )}
     * which transform the hits into {@link Film} objects.
     *
     * @param params An String array containing the params of the search
     * @throws IOException If the method can't serialize the hit into a Film JSON or an error occur while searching
     *                     the query through the client.
     */
    @Override
    public QueryResponse searchByParams(String[] params) throws IOException {
        var request = new SearchRequest("imdb");

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        buildBoolQuery(params, queryBuilder);

        request.source(getSearchSourceBuilder(queryBuilder));

        return getQueryResponse(request);
    }

    private void buildBoolQuery(String[] params, BoolQueryBuilder queryBuilder) {
        MatchQueryBuilder matchTitleQueryBuilder, matchTypeQueryBuilder, matchGenreQueryBuilder;

        if (params[0].length() >= 1) {
            matchTitleQueryBuilder = new MatchQueryBuilder("title", params[0]);
            queryBuilder.should(matchTitleQueryBuilder);
        }
        if (params[1].length() >= 1) {
            matchGenreQueryBuilder = new MatchQueryBuilder("genres", params[1]);
            queryBuilder.should(matchGenreQueryBuilder);
        }
        if (params[2].length() >= 1) {
            matchTypeQueryBuilder = new MatchQueryBuilder("type", params[2]);
            queryBuilder.should(matchTypeQueryBuilder);
        }
        if (params[3].length() >= 1 && params[3].matches("([0-9]{4}-[0-9]{4},*)+")) {
            var dates = params[3].split(",");
            var years = dates[0].split("-");
            var rangeQueryBuilder = new RangeQueryBuilder("start_year");
            rangeQueryBuilder.format("yyyy");
            rangeQueryBuilder.gte(years[0]);
            rangeQueryBuilder.lte(years[1]);
            queryBuilder.must(rangeQueryBuilder);

        }

    }


    /**
     * Helper method, builds a {@see SearchSourceBuilder} from a {@see QueryBuilder} passed by params and then
     * configure it.
     *
     * @param queryBuilder The QueryBuilder we want to transform into a SearchSourceBuilder.
     * @return The SearchSourceBuilder properly configured.
     */
    private SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder) {
        var sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.size(10);
        addAggregations(sourceBuilder);

        return sourceBuilder.query(queryBuilder);
    }

    /**
     * Helper method which adds aggregations to the sourceBuilder
     *
     * @param sourceBuilder The sourceBuilder to be modified
     */
    private void addAggregations(SearchSourceBuilder sourceBuilder) {
        var aggTermsGenresBuilder = AggregationBuilders.terms("genres");
        var aggTypeTermsBuilder = AggregationBuilders.terms("types");

        aggTermsGenresBuilder.field("genres");
        aggTypeTermsBuilder.field("type");

        sourceBuilder.aggregation(aggTermsGenresBuilder).aggregation(aggTypeTermsBuilder);
    }

    /**
     * Helper method, do the search request using the {@see ElasticClient} and transform the response into the
     * normalized response POJO {@see QueryResponse}.
     *
     * @param request The pre-built SearchRequest that is gonna be called.
     * @return A QueryResponse POJO with the corresponding data retrieved from the response.
     * @throws IOException If there is a problem while performing the search request.
     */
    private QueryResponse getQueryResponse(SearchRequest request) throws IOException {

        var response = client.getClient().search(request, RequestOptions.DEFAULT);
        long total = response.getHits().getTotalHits().value;
        Film[] films = parseHitToFilms(response.getHits());

        objectMapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);

        Terms genres = response.getAggregations().get("genres");
        Terms types = response.getAggregations().get("types");

        TermAggregationPojo[] aggregationPojos = new TermAggregationPojo[2];
        aggregationPojos[0] = getTermAggregationPojo(genres.getBuckets(), "genres");
        aggregationPojos[1] = getTermAggregationPojo(types.getBuckets(), "types");

        return new QueryResponse(total, films, aggregationPojos);
    }

    /**
     * @param buckets The original buckets from Elastic Search
     * @param name    The name of the Term Aggregation POJO to be returned
     * @return A Term Aggregation POJO with the name and buckets received throught params {@see TermAggregationPojo}
     */
    private TermAggregationPojo getTermAggregationPojo(List<? extends Terms.Bucket> buckets, String name) {
        var termBuckets = transformBucketToPojo(buckets);
        return new TermAggregationPojo(name, termBuckets);
    }

    /**
     * Helper method, transform and original bucket into a serializable POJO bucket.
     *
     * @param originalBuckets Original bucket from Elastic Search
     * @return POJO bucket {@see TermBucketPojo}
     */
    private TermBucketPojo[] transformBucketToPojo(List<? extends Terms.Bucket> originalBuckets) {
        return originalBuckets.stream()
                .map(bucket -> new TermBucketPojo(bucket.getKeyAsString(), bucket.getDocCount()
                )).toArray(TermBucketPojo[]::new);
    }

    /**
     * Helper method which transforms the elastic search response hits into Film objects.
     *
     * @param hits The hits of the search
     * @return An array of films {@see Film}
     */
    private Film[] parseHitToFilms(SearchHits hits) {
        return Arrays.stream(hits.getHits()).map(hit -> {
            String source = hit.getSourceAsString();
            try {
                return objectMapper.readValue(source, Film.class);
            } catch (JsonProcessingException e) {
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }).toArray(Film[]::new);
    }

    /**
     * Helper method, takes an {@link InputStream} and return a {@link BufferedReader}
     *
     * @param dataPath The InputStream pointing to the file
     * @return The BufferedReader of the file
     */
    private BufferedReader readFile(InputStream dataPath) {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(dataPath, StandardCharsets.UTF_8));
        return reader;
    }

}
