package com.dfa.imdb_search_api.elastic.commands.impl;

import com.dfa.imdb_search_api.POJO.Rating;
import com.dfa.imdb_search_api.elastic.commands.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;

public class RatingBulkCreationCommand implements Command {

    ObjectMapper objectMapper;

    @Override
    public void execute(String line, BulkRequest bulk, ObjectMapper objectMapper) throws IOException {
        Rating rating = createRatingFromLine(line);
        String ratingJson = objectMapper.writeValueAsString(rating);
        bulk.add(new UpdateRequest("imdb", rating.getId())
                .doc(ratingJson, XContentType.JSON)
                .upsert(ratingJson, XContentType.JSON));
    }

    /**
     * Helper method, creates a {@link Rating} POJO from a line extracted from the data source.
     *
     * @param line String containing the info of the rating separated by tabs.
     * @return A Rating POJO
     */
    private Rating createRatingFromLine(String line) {
        var data = line.split("\t");
        return new Rating(data[0], Float.parseFloat(data[1]), Integer.parseInt(data[2]));
    }

}
