package com.dfa.imdb_search_api.elastic.commands.impl;

import com.dfa.imdb_search_api.POJO.Film;
import com.dfa.imdb_search_api.elastic.commands.Command;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.xcontent.XContentType;

public class FilmBulkCreationCommand implements Command {

    @Override
    public void execute(String line, BulkRequest bulk, ObjectMapper objectMapper) throws JsonProcessingException {
        Film film = createFilmFromLine(line);
        String filmJson = objectMapper.writeValueAsString(film);
        bulk.add(new UpdateRequest("imdb", film.getId())
                .doc(filmJson, XContentType.JSON)
                .upsert(filmJson, XContentType.JSON));
    }

    /**
     * Helper method, creates a {@link Film} POJO from a line extracted from the data source.
     *
     * @param line String containing the info of the film separated by tabs.
     * @return A Film POJO
     */
    private Film createFilmFromLine(String line) {
        var data = line.split("\t");
        return new Film(data[0], data[2], data[3],
                data[8].equals("\\N") ? null : data[8].split(","),
                data[1],
                data[5].equals("\\N") ? null : data[5] + "-01-01",
                data[6].equals("\\N") ? null : data[6] + "-01-01");
    }

}
