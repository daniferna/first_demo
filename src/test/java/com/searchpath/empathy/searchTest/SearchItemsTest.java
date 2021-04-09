package com.searchpath.empathy.searchTest;

import com.searchpath.empathy.elastic.util.IElasticUtil;
import com.searchpath.empathy.pojo.Film;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class SearchItemsTest {

    private final IElasticUtil elasticUtil;

    @Inject
    SearchItemsTest(@Named("ElasticClientUtil") IElasticUtil elasticUtil) {
        this.elasticUtil = elasticUtil;
    }

    @Test
    public void testSearchQuerySpiderman() throws IOException {
        var expectedSpidermanFilm = new Film("tt0413300", "Spider-Man 3"
                , new String[]{"Action", "Adventure", "Sci-Fi"}, "movie", "2007-01-01", null);

        var response = elasticUtil.search("Spiderman III");
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));

        response = elasticUtil.search("Spiderman 3");
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));

        response = elasticUtil.search("Spider-man 3 movie");
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));
    }

    @Test
    public void testSearchTitleSpiderman() throws IOException {
        var expectedSpidermanFilm = new Film("tt0413300", "Spider-Man 3"
                , new String[]{"Action", "Adventure", "Sci-Fi"}, "movie", "2007-01-01", null);

        var response = elasticUtil.searchByParams(Map.of("query", "Spiderman 3"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));

        response = elasticUtil.searchByParams(Map.of("query", "Spiderman III"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));

        response = elasticUtil.searchByParams(Map.of("query", "Spider-man III"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));
    }

    @Test
    public void testSearchTitleAndGenreSpiderman() throws IOException {
        var expectedSpidermanFilm = new Film("tt0413300", "Spider-Man 3"
                , new String[]{"Action", "Adventure", "Sci-Fi"}, "movie", "2007-01-01", null);

        var response = elasticUtil.searchByParams(
                Map.of("query", "Spiderman 3","genres", "Action,adventure,Sci-Fi"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));
    }

    @Test
    public void testSearchTitleGenreAndTypeSpiderman() throws IOException {
        var expectedSpidermanFilm = new Film("tt0413300", "Spider-Man 3"
                , new String[]{"Action", "Adventure", "Sci-Fi"}, "movie", "2007-01-01", null);

        var response = elasticUtil.searchByParams(
                Map.of("query", "Spiderman 3", "genres", "Action,adventure,Sci-Fi", "type", "movie"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedSpidermanFilm));
    }

    @Test
    public void testSearchQueryStandardFilm() throws IOException {
        var expectedFilm = new Film("tt5726616", "Call Me by Your Name", new String[]{"Drama", "Romance"}
                , "movie", "2017-01-01", null);

        var response = elasticUtil.search("Call me by your name");
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchTitleStandardFilm() throws IOException {
        var expectedFilm = new Film("tt5726616", "Call Me by Your Name", new String[]{"Drama", "Romance"}
                , "movie", "2017-01-01", null);

        var response = elasticUtil.searchByParams(Map.of("query", "Call me by your name"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchTitleAndGenreStandardFilm() throws IOException {
        var expectedFilm = new Film("tt5726616", "Call Me by Your Name", new String[]{"Drama", "Romance"}
                , "movie", "2017-01-01", null);

        var response = elasticUtil.searchByParams(Map.of("query", "Call me by your name", "genres", "Drama,romance"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchTitleGenreAndTypeStandardFilm() throws IOException {
        var expectedFilm = new Film("tt5726616", "Call Me by Your Name", new String[]{"Drama", "Romance"}
                , "movie", "2017-01-01", null);

        var response = elasticUtil.searchByParams(
                Map.of("query", "Call me by your name","genres", "Drama,romance", "type", "movie"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchCorrectDateStandardFilm() throws IOException {
        var expectedFilm = new Film("tt5726616", "Call Me by Your Name", new String[]{"Drama", "Romance"}
                , "movie", "2017-01-01", null);

        var response = elasticUtil.searchByParams(
                Map.of("query", "Call me by your name","genres", "Drama,romance", "type", "movie", "date", "2016-2018"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchIncorrectDateStandardFilm() throws IOException {
        var expectedFilm = new Film("tt5726616", "Call Me by Your Name", new String[]{"Drama", "Romance"}
                , "movie", "2017-01-01", null);

        var response = elasticUtil.searchByParams(
                Map.of("query", "Call me by your name", "genres", "Drama,romance", "type", "movie", "date", "2010-2013,2000-2004"));
        assertFalse(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchQueryMassiveFilm() throws IOException {
        var expectedFilm = new Film("tt4154796", "Avengers: Endgame"
                , new String[]{"Action", "Adventure", "Drama"}, "movie",
                "2019-01-01", null);

        var response = elasticUtil.search("Avengers: Endgame movie");
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));

        response = elasticUtil.search("Avengers:Endgame movie");
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchTitleMassiveFilm() throws IOException {
        var expectedFilm = new Film("tt4154796", "Avengers: Endgame"
                , new String[]{"Action", "Adventure", "Drama"}, "movie",
                "2019-01-01", null);

        var response = elasticUtil.searchByParams(Map.of("query", "Avengers: Endgame"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchTitleAndTypeMassiveFilm() throws IOException {
        var expectedFilm = new Film("tt4154796", "Avengers: Endgame"
                , new String[]{"Action", "Adventure", "Drama"}, "movie",
                "2019-01-01", null);

        var response = elasticUtil.searchByParams(Map.of("query", "Avengers: Endgame", "type", "movie"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

    @Test
    public void testSearchTitleGenreAndTypeMassiveFilm() throws IOException {
        var expectedFilm = new Film("tt4154796", "Avengers: Endgame"
                , new String[]{"Action", "Adventure", "Drama"}, "movie",
                "2019-01-01", null);

        var response = elasticUtil.searchByParams(
                Map.of("query", "Avengers: Endgame", "genres", "Action,Adventure,Drama", "type", "movie"));
        assertTrue(Arrays.asList(response.getItems()).contains(expectedFilm));
    }

}
