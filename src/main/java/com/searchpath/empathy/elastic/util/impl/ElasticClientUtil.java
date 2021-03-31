package com.searchpath.empathy.elastic.util.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.searchpath.empathy.elastic.ElasticClient;
import com.searchpath.empathy.elastic.commands.Command;
import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import com.searchpath.empathy.pojo.QueryResponse;
import com.searchpath.empathy.pojo.aggregations.Aggregation;
import com.searchpath.empathy.pojo.aggregations.bucket.impl.DateHistogramBucket;
import com.searchpath.empathy.pojo.aggregations.bucket.impl.TermBucket;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
     * This implementation reads the file {@link #readFile(InputStream)} and then, it divides the entry into chunks
     * of several lines with the help of the Guava's Iterators library help. {@link Iterators#partition(Iterator, int)}
     * <p>
     * Following that, read each chunk and proceeds to create a object which is then deserialized into
     * a Json and added to a {@link BulkRequest}. Next step is take the elastic search client and upload this bulk.
     * For doing that, the method receive {@link Command} containing all the logic necessary in that case.
     * <p>
     * This is repeat for every chunk.
     *
     * @throws IOException If the method can't deserialize the film object into JSON
     *                     or an error occur while loading the bulk data through the client.
     */
    @Override
    public String loadIMDBMedia(String fileName, int chunkSize, Command command) throws IOException {
        if (!client.getClient().indices().exists(new GetIndexRequest("imdb"), RequestOptions.DEFAULT))
            createIndex();

        var reader = readFile(this.getClass().getClassLoader().getResourceAsStream(fileName));
        var bulk = new BulkRequest();

        // Split the data stream in chunks in a lazy way.
        UnmodifiableIterator<List<String>> linesList = Iterators.partition(reader.lines().skip(1).iterator(), chunkSize);

        while (linesList.hasNext()) {
            List<String> list = linesList.next();

            for (String line : list) {
                command.execute(line, bulk, objectMapper);
            }

            client.getClient().bulk(bulk, RequestOptions.DEFAULT);
            bulk = new BulkRequest();
        }

        reader.close();
        return "Success loading data";
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

        request.source(getSearchSourceBuilder(getSearchQueryBuilder(query)));

        return getQueryResponse(request);
    }

    /**
     * Helper method, it builds the main search query, the general one.
     *
     * @param query Text containing the information we are looking for.
     * @return The {@link FunctionScoreQueryBuilder} ready to be passed to the request.
     */
    private FunctionScoreQueryBuilder getSearchQueryBuilder(String query) {
        var multiMatchQueryBuilder = new MultiMatchQueryBuilder(query, "title", "genres", "type", "start_year.getYear");
        multiMatchQueryBuilder.field("title", 3);
        multiMatchQueryBuilder.field("type", 2);
        multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.MOST_FIELDS);

        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctions = getFilterFunctions(query);

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                .functionScoreQuery(multiMatchQueryBuilder, filterFunctions);
        functionScoreQueryBuilder.boost(5);
        functionScoreQueryBuilder.boostMode(CombineFunction.MULTIPLY);

        return functionScoreQueryBuilder;
    }

    /**
     * Helper method, build all the filter functions to improve the query search results.
     *
     * @return An array containing all the filter functions
     */
    private FunctionScoreQueryBuilder.FilterFunctionBuilder[] getFilterFunctions(String query) {
        return new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                // Start year gauss decay function
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders
                        .gaussDecayFunction("start_year", "now", "3650d", "0d", 0.7)),
                // Boost movies results over other types
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new MatchQueryBuilder("type", "movie"),
                        ScoreFunctionBuilders.weightFactorFunction(1.8f)),
                // Boost tvSeries results over other types
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new MatchQueryBuilder("type", "tvSeries"),
                        ScoreFunctionBuilders.weightFactorFunction(1.3f)),
                // Boost shorts results over other types
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new MatchQueryBuilder("type", "short"),
                        ScoreFunctionBuilders.weightFactorFunction(1.2f)),
                // Reduce result of type tvEpisode
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new MatchQueryBuilder("type", "tvEpisode"),
                        ScoreFunctionBuilders.weightFactorFunction(0.2f)),
                // Boost exact matches of titles
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new MatchPhraseQueryBuilder("title", query),
                        ScoreFunctionBuilders.weightFactorFunction(1.2f)),
                // Boost the results with higher avg rating
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders
                        .fieldValueFactorFunction("average_rating").factor(1.1f)
                        .modifier(FieldValueFactorFunction.Modifier.LOG1P).missing(1)),
                // Boost the results with higher number of votes
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders
                        .fieldValueFactorFunction("num_votes").factor(1.5f)
                        .modifier(FieldValueFactorFunction.Modifier.LOG1P).missing(1))
        };
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
        MatchQueryBuilder matchTypeQueryBuilder;

        //Make sure the array has the appropriate length
        params = Arrays.stream(Arrays.copyOf(params, 4))
                .map(str -> str == null ? "" : str).toArray(String[]::new);

        var generalSearchQueryBuilder = getSearchQueryBuilder(params[0]);
        queryBuilder.must(generalSearchQueryBuilder);

        if (params[1].length() >= 1) {
            for (var genre : params[1].split(",")) {
                queryBuilder.filter(new TermQueryBuilder("genres", genre));
            }
        }
        if (params[2].length() >= 1) {
            matchTypeQueryBuilder = new MatchQueryBuilder("type", params[2]);
            queryBuilder.filter(matchTypeQueryBuilder);
        }
        if (params[3].length() >= 1 && params[3].matches("([0-9]{4}-[0-9]{4},*)+")) {
            BoolQueryBuilder boolQueryBuilder = buildDateQuery(params[3]);
            queryBuilder.filter(boolQueryBuilder);
        }

    }

    /**
     * Helper method, transform a valid date param into a {@link BoolQueryBuilder} containing
     * various {@link RangeQueryBuilder}, united by the should clause.
     *
     * @param paramDates String containing a valid succession of date ranges (following regex: ([0-9]{4}-[0-9]{4},*)+
     * @return A BoolQueryBuilder containing various RangeQueryBuilder
     */
    private BoolQueryBuilder buildDateQuery(String paramDates) {
        var dates = paramDates.split(",");
        var boolQueryBuilder = new BoolQueryBuilder();

        for (var date : dates) {
            var years = date.split("-");
            var rangeQueryBuilder = new RangeQueryBuilder("start_year");
            rangeQueryBuilder.format("yyyy");
            rangeQueryBuilder.gte(years[0]);
            rangeQueryBuilder.lte(years[1]);
            boolQueryBuilder.should(rangeQueryBuilder);
        }

        return boolQueryBuilder;
    }


    /**
     * Helper method, builds a {@link SearchSourceBuilder} from a {@link QueryBuilder} passed by params and then
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
        var aggDateHistogramBuilder = AggregationBuilders.dateHistogram("decades");

        aggTermsGenresBuilder.field("genres");
        aggTypeTermsBuilder.field("type");

        aggDateHistogramBuilder.field("start_year");
        //Use of seconds in order to minimize the problem generated by leap-years
        aggDateHistogramBuilder.fixedInterval(new DateHistogramInterval("315581500s"));
        aggDateHistogramBuilder.format("yyyy");
        aggDateHistogramBuilder.minDocCount(0);
        aggDateHistogramBuilder.offset("2h");

        sourceBuilder.aggregation(aggTermsGenresBuilder).aggregation(aggTypeTermsBuilder)
                .aggregation(aggDateHistogramBuilder);
    }

    /**
     * Helper method, do the search request using the {@link ElasticClient} and transform the response into the
     * normalized response POJO {@link QueryResponse}.
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
        Histogram dateHistogram = response.getAggregations().get("decades");

        Aggregation<DateHistogramBucket> dateHistogramAggregation =
                getDateHistogramAggregationPojo(dateHistogram.getBuckets());

        Aggregation<TermBucket>[] termAggregations = new Aggregation[2];
        termAggregations[0] = getTermAggregationPojo(genres.getBuckets(), "genres");
        termAggregations[1] = getTermAggregationPojo(types.getBuckets(), "types");

        return new QueryResponse(total, films, termAggregations, dateHistogramAggregation);
    }

    /**
     * @param buckets The original buckets from Elastic Search
     * @return A Date Histogram Aggregation POJO with the buckets received through params {@link Aggregation<DateHistogramBucket>}
     */
    private Aggregation<DateHistogramBucket> getDateHistogramAggregationPojo(List<? extends Histogram.Bucket> buckets) {
        var dateHistogramBuckets = transformDateHistogramBucketsToPojo(buckets);
        return new Aggregation<>("decades", dateHistogramBuckets);
    }

    /**
     * Helper method, transform original bucket into a serializable POJO bucket.
     *
     * @param originalBuckets Original bucket from Elastic Search
     * @return POJO bucket {@link DateHistogramBucket}
     */
    private DateHistogramBucket[] transformDateHistogramBucketsToPojo(List<? extends Histogram.Bucket> originalBuckets) {
        return originalBuckets.stream()
                .map(bucket -> new DateHistogramBucket(bucket.getDocCount(), bucket.getKeyAsString()))
                .toArray(DateHistogramBucket[]::new);
    }

    /**
     * @param buckets The original buckets from Elastic Search
     * @param name    The name of the Term Aggregation POJO to be returned
     * @return A Term Aggregation POJO with the name and buckets received through params {@link Aggregation<TermBucket>}
     */
    private Aggregation<TermBucket> getTermAggregationPojo(List<? extends Terms.Bucket> buckets, String name) {
        var termBuckets = transformTermBucketsToPojo(buckets);
        return new Aggregation<>(name, termBuckets);
    }

    /**
     * Helper method, transform original bucket into a serializable POJO bucket.
     *
     * @param originalBuckets Original bucket from Elastic Search
     * @return POJO bucket {@link TermBucket}
     */
    private TermBucket[] transformTermBucketsToPojo(List<? extends Terms.Bucket> originalBuckets) {
        return originalBuckets.stream()
                .map(bucket -> new TermBucket(bucket.getKeyAsString(), bucket.getDocCount()
                )).toArray(TermBucket[]::new);
    }

    /**
     * Helper method which transforms the elastic search response hits into Film objects.
     *
     * @param hits The hits of the search
     * @return An array of films {@link Film}
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
