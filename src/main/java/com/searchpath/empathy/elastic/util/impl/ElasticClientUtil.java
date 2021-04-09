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
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
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
        UnmodifiableIterator<List<String>> linesList =
                Iterators.partition(reader.lines().skip(1).iterator(), chunkSize);

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
     * Also adds the needed filter functions.
     * <p>
     * If the query param is an empty string, it performs a match_all query.
     * If not, it performs a multi match query.
     *
     * @param query Text containing the information we are looking for.
     * @return The {@link FunctionScoreQueryBuilder} ready to be passed to the request.
     */
    private FunctionScoreQueryBuilder getSearchQueryBuilder(String query) {
        var multiMatchQueryBuilder = new MultiMatchQueryBuilder(query, "title", "original_title");
        multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.BEST_FIELDS);

        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctions = getFilterFunctions(query);

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                .functionScoreQuery(multiMatchQueryBuilder, filterFunctions);

        //If the query is empty, it performs a match all
        if (query.isEmpty())
            functionScoreQueryBuilder = QueryBuilders
                    .functionScoreQuery(new MatchAllQueryBuilder(), filterFunctions);

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
                // Boost exact matches of titles or original titles
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new DisMaxQueryBuilder().add(new MatchPhraseQueryBuilder("title", query))
                                .add(new MatchPhraseQueryBuilder("original_title", query))
                                .tieBreaker(0.2f),
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
    public QueryResponse searchByParams(Map<String, String> params) throws IOException {
        var request = new SearchRequest("imdb");

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        buildBoolQuery(params, queryBuilder);

        request.source(getSearchSourceBuilder(queryBuilder,
                params.getOrDefault("filters", "").split(",")));

        return getQueryResponse(request);
    }

    /**
     * This implementation use the {@link ElasticClient} API to look for the title
     * specified by the id passed by params.
     * It creates a {@link SearchRequest}, and using a {@link QueryBuilders#matchPhraseQuery(String, Object),
     * passing as params the name of the field and the id received by params.
     * <p>
     * Once the response is obtained, is processed by a private helper method {@link #parseHitToFilms(SearchHits)}
     * which transform the hits, just one in this case, into {@link Film} object, which is then returned.
     *
     * @throws IOException If the method can't serialize the hit into a Film JSON or an error occur while searching
     *                     the query through the client.
     */
    @Override
    public Film searchByTitleID(String id) throws IOException {
        var request = new SearchRequest("imdb");

        var query = QueryBuilders.matchPhraseQuery("id", id);

        request.source(getSearchSourceBuilder(query, false));

        var response = client.getClient().search(request, RequestOptions.DEFAULT);

        return parseHitToFilms(response.getHits())[0];
    }

    /**
     * Helper method, it builds a {@link org.apache.lucene.search.BooleanQuery} containing all the filters needed, having into account the
     * params received as parameters.
     *
     * @param params       A String array containing all the fields that will be used in the bool query as filters
     * @param queryBuilder The builder containing all the information needed to build the Boolean Query.
     */
    private void buildBoolQuery(Map<String, String> params, BoolQueryBuilder queryBuilder) {
        var generalSearchQueryBuilder = getSearchQueryBuilder(params.get("query"));
        queryBuilder.must(generalSearchQueryBuilder);

        if (!params.getOrDefault("genres", "").isBlank())
            for (var genre : params.get("genres").split(","))
                queryBuilder.filter(new TermQueryBuilder("genres", genre));

        if (!params.getOrDefault("type", "").isBlank())
            queryBuilder.filter(new MatchQueryBuilder("type", params.get("type")));

        if (!params.getOrDefault("date", "").isBlank()
                && params.get("date").matches("([0-9]{4}-[0-9]{4},*)+"))
            queryBuilder.filter(buildDateQuery(params.get("date")));
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
     * @param queryBuilder     The QueryBuilder we want to transform into a SearchSourceBuilder.
     * @param filters          The filters you want to apply as post filters.
     * @param needAggregations A boolean value defining if there is a need for aggregations or not.
     * @return The SearchSourceBuilder properly configured.
     */
    private SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, String[] filters
            , boolean needAggregations) {
        var sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.size(10);
        if (needAggregations)
            addAggregations(sourceBuilder, filters);
        addPostFilters(filters, sourceBuilder);

        return sourceBuilder.query(queryBuilder);
    }

    /**
     * Implementation of default parameter method using method overloading.
     *
     * @param queryBuilder The QueryBuilder we want to transform into a SearchSourceBuilder.
     * @return The SearchSourceBuilder properly configured.
     */
    private SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder) {
        return this.getSearchSourceBuilder(queryBuilder, new String[]{}, true);
    }

    /**
     * Implementation of default parameter method using method overloading.
     *
     * @param queryBuilder The QueryBuilder we want to transform into a SearchSourceBuilder.
     * @param filters      The filters you want to apply as post filters.
     * @return The SearchSourceBuilder properly configured.
     */
    private SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, String[] filters) {
        return this.getSearchSourceBuilder(queryBuilder, filters, true);
    }

    /**
     * Implementation of default parameter method using method overloading.
     *
     * @param queryBuilder     The QueryBuilder we want to transform into a SearchSourceBuilder.
     * @param needAggregations A boolean value defining if there is a need for aggregations or not.
     * @return The SearchSourceBuilder properly configured.
     */
    private SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, boolean needAggregations) {
        return this.getSearchSourceBuilder(queryBuilder, new String[]{}, needAggregations);
    }

    /**
     * Helper method which adds post filters to the sourceBuilder
     *
     * @param filters       Filters to be added
     * @param sourceBuilder The source builder to be modified
     */
    private void addPostFilters(String[] filters, SearchSourceBuilder sourceBuilder) {
        var boolQuery = QueryBuilders.boolQuery();
        for (var filterStr : filters) {
            var filter = filterStr.split(":");
            switch (filter[0]) {
                case "genres", "type" -> boolQuery.filter().add(QueryBuilders.termQuery(filter[0], filter[1]));
                case "date" -> {
                    var dateRange = filter[1].split("-");
                    boolQuery.filter().add(QueryBuilders.
                            rangeQuery("start_year").format("yyyy")
                            .gte(dateRange[0]).lte(dateRange[1]));
                }
            }
        }
        sourceBuilder.postFilter(boolQuery);
    }

    /**
     * Helper method which adds aggregations to the sourceBuilder
     *
     * @param sourceBuilder The sourceBuilder to be modified
     */
    private void addAggregations(SearchSourceBuilder sourceBuilder, String[] filters) {
        //Declaration of subAggregations
        var aggTermsGenresBuilder = AggregationBuilders.terms("genres");
        var aggTypeTermsBuilder = AggregationBuilders.terms("types");
        var aggDateHistogramBuilder = AggregationBuilders.dateHistogram("decades");

        //Configuration of subAggregations
        aggTermsGenresBuilder.field("genres")
                .size(28);
        aggTypeTermsBuilder.field("type")
                .size(13);
        aggDateHistogramBuilder.field("start_year")
                //Use of seconds in order to minimize the problem generated by leap-years
                .fixedInterval(new DateHistogramInterval("315581500s"))
                .format("yyyy")
                .offset("2h");

        //Creation and population of filters
        Map<String, BoolQueryBuilder> queryBuildersFilterMap = getBoolQueryBuilderMap();
        populateQueryBuildersWithFilters(filters, queryBuildersFilterMap);

        //Declaration and creation of aggregations
        var filteredAggregationGenres = AggregationBuilders
                .filter("genres_filter", queryBuildersFilterMap.get("genres"))
                .subAggregation(aggTermsGenresBuilder);
        var filteredAggregationTypes = AggregationBuilders
                .filter("types_filter", queryBuildersFilterMap.get("type"))
                .subAggregation(aggTypeTermsBuilder);
        var filteredAggregationDecades = AggregationBuilders
                .filter("decades_filter", queryBuildersFilterMap.get("decades"))
                .subAggregation(aggDateHistogramBuilder);

        //Addition of aggregations to the source builder
        sourceBuilder.aggregation(filteredAggregationDecades).aggregation(filteredAggregationTypes)
                .aggregation(filteredAggregationGenres);
    }

    /**
     * Helper method, creates a dictionary containing a {@link BoolQueryBuilder} per each type of aggregation.
     *
     * @return A {@link Map} containing empty BoolQueryBuilders.
     */
    private Map<String, BoolQueryBuilder> getBoolQueryBuilderMap() {
        Map<String, BoolQueryBuilder> queryBuildersFilterDic = new HashMap<>();
        queryBuildersFilterDic.put("decades", QueryBuilders.boolQuery());
        queryBuildersFilterDic.put("type", QueryBuilders.boolQuery());
        queryBuildersFilterDic.put("genres", QueryBuilders.boolQuery());
        return queryBuildersFilterDic;
    }

    /**
     * Helper method, receive a list of filters and a {@link Map} containing {@link BoolQueryBuilder}, each one
     * relate to one Aggregation. Then populate each BoolQueryBuilder with the corresponding filters, those who have different
     * field than the boolQueryBuilder. Example: Having three filters. Filter "decades" populate queryBuilders "type" and "genres".
     *
     * @param filters                Filters used to populate the BoolQueryBuilder
     * @param queryBuildersFilterMap Map containing the field name and their corresponding BoolQueryBuilder
     */
    private void populateQueryBuildersWithFilters(String[] filters,
                                                  Map<String, BoolQueryBuilder> queryBuildersFilterMap) {
        for (var filterStr : filters) {
            var filter = filterStr.split(":");
            switch (filter[0]) {
                case "decades" -> {
                    var dateRange = filter[1].split("-");
                    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.
                            rangeQuery("start_year").format("yyyy")
                            .gte(dateRange[0]).lte(dateRange[1]);
                    queryBuildersFilterMap.forEach((k, v) -> {
                        if (!k.equals("decades"))
                            v.filter().add(rangeQueryBuilder);
                    });
                }
                case "type", "genres" -> queryBuildersFilterMap.forEach((k, v) -> {
                    if (!k.equals(filter[0]))
                        v.filter().add(QueryBuilders.termQuery(filter[0], filter[1]));
                });
            }
        }
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

        Terms genres = ((ParsedFilter) response.getAggregations().get("genres_filter")).getAggregations().get("genres");
        Terms types = ((ParsedFilter) response.getAggregations().get("types_filter")).getAggregations().get("types");
        Histogram dateHistogram = ((ParsedFilter) response.getAggregations()
                .get("decades_filter")).getAggregations().get("decades");

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
