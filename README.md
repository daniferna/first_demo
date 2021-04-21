# Search path project

This is a demo of a Micronaut API Client which manage petitions to an elasticsearch client done as part of my internship
at Empathy.co

It uses data from IMDB.

## Before the start

Make sure you have installed in your system the following items:

* Java 15
* Docker Desktop CE
* Gradle 6.4.1

I recommend using SDKMan for the installation, but is not mandatory.

```bash
curl -s "https://get.sdkman.io" | bash
sdk list java
#Choose the Java v11 you like the most, I use java 11.0.3.hs-adpt
sdk install java <the one you choosed>
sdk install gradle 6.4.1
```

## First steps

### Elastic search deployment

To start using the search engine we need to deploy our elasticsearch engine. This can be easily done using Docker.

1. Open a terminal and download the elasticsearch docker image:<br/>

```bash   
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.11.1
```

2. Deploy this image using the ports 9200 and 9300:<br/>

```bash   
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.11.1
```

In successive deployments you just need to deploy through the Docker Desktop app or through the following command:
<br/><code>docker start elasticsearch</code>

### Micronaut API client deployment

Once we have the elasticsearch container running, we can proceed with the execution of the API which will manage the
petitions to elasticsearch.

1. Execute the following command in the root directory of the project:<br/>

```bash   
./gradlew run
```

### Indexing the IMDB data

We use the IMDB data available at https://datasets.imdbws.com, more specifically the *title.basics.tsv.gz* and
_title.ratings.tsv.gz_. This dataset includes information as:

* Title of the media
* Type of media
* Genre of media
* Start date of emission
* End date of emission
* Average rating
* Number of votes

In order to index this information on elasticsearch we need to follow the next steps:

1. Make sure you have all the appropriate dataset files in the **/src/resources** folder named as **films.tsv** and
   **ratings.tsv**
2. Open a terminal and execute the following:<br/>

```bash
curl -XGET "localhost:8080/index"
```

Or, as an alternative, open a web browser and go to the following url:
[localhost:8080/index](localhost:8080/index)

Once the index process is finished you will see a "success" message appear in the bash or the browser. It can take a
while, so be patient. It normally takes less than 10 minutes, but it depends on the computer.

#### Other indexing methods

It is also possible to index just one of those datasets, this can be done through the following API endpoints:

* /index/films
* /index/ratings

There is another endpoint which performs the indexing in the background, without having the terminal or the web browser
on hold. Once finished, it will print a notification on the terminal running Micronaut.

This endpoint is **/index/background**. Example of use:

```bash
curl -XGET "localhost:8080/index/background"
```

## Types of fields

The indexed data fit for search has the following types and values:

* Title: text
    * Passed under *query* parameter
* Type: text
    * Possible values:
        * movie
        * short
        * video
        * tvSeries
        * tvEpisode
        * tvMovie
        * tvSpecial
        * tvMiniseries
        * tvShort
        * videogame
        * audiobook
        * radioseries
        * episode
* Genre: text
    * Possible values:
        * drama
        * comedy
        * short
        * talk-show
        * documentary
        * romance
        * family
        * news
        * reality-tv
        * animation
        * music
        * crime
        * action
        * adventure
        * game-show
        * adult
        * sport
        * fantasy
        * mystery
        * horror
        * thriller
        * history
        * sci-fi
        * biography
        * musical
        * war
        * western
        * film-noir
* Date: date
    * The input format is:
        * yyyy-yyyy,...
        * Example:<br/>
          1980-1990 <br/>or<br/> 1980-1990, 2000-2010,...
    * Internally saved as *start_year*

## What can you do with the API

All the calls to the API are HTTP GET calls being listened at /search. The parameters of the call are passed by queries
at the url:
> http:localhost:8080/search?query=*text*

or

> http:localhost:8080/search?*query=Spiderman+3&type=movie*

### Types of search

Right now, you can do the following types of queries:

#### Search by query

* This kind of search receive a piece of text and performs a general search trying to extract and match all the elements
  of the query with the info elasticsearch has saved.
* Example:
  > http:localhost:8080/search?query=Avengers:+Endgame

#### Search by query + fields

This kind of search looks specifically for the fields passed by parameters. Gives better results than the general query
if you know the values and type of the elements you're passing by parameters.
* The fields can be combined
* The fields can be the following:
    * title
    * type
    * genres
        * One or more values separated by commas
    * date
        * One or more range of years separated by commas

##### Examples of search by query + fields:

Films titled "Spiderman 3"
> /search?query=Spiderman+3&type=movie

Media titled "The great adventure" of genre drama and adventure
> /search?query=The+great+adventure&genres=drama,adventure

Film titled "Call me by your name" of genre drama and romance released between 2010 and 2020
> /search?query=Call+me+by+your+name&type=movie&genres=drama,romance&date=2010-2020

Film of genre drama released between 2010 and 2013, and between 1980 and 1995
> /search?query=The+Schindler's+list&genres=drama&date=2010-2013,1980-1995

#### Search by query and filters

This king of search looks for the query passed by params and use the filter field as
a [post-filter](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/filter-search-results.html#post-filter).

It's similar to the query + fields kind of search but this way the filters are applied after the search is done.

This way the aggregations behave differently:

When a filter is applied, the aggregations of the field being used as a filter are not affected by it, but the rest of
aggregations are.

* The filters can be combined
* The filters can use the following fields:
    * title
    * type
    * genre
        * Only one per filter.
    * date
        * Only one range of years separated by commas

##### Examples of search by query + filters

Films titled "Spiderman 3"
> /search?query=Spiderman+3&filter=type:movie

Media titled "The great adventure" of genre drama and adventure
> /search?query=The+great+adventure&filter=genres:drama&filter=genres:adventure

Film titled "Call me by your name" of genre drama and romance released between 2010 and 2020
> /search?query=Call+me+by+your+name&filter=type:movie&filter=genres:drama&filter=genres:romance&filter=date:2010-2020

Film of genre drama released between 2010 and 2013, and between 1980 and 1995
> /search?query=The+Schindler's+list&filter=genre:drama&filter=date:2010-2013&filter=date:1980-1995

### What does the API return

The API search endpoints always return a response in JSON format containing the following fields:

1. "total": Number of results matching the query. Limit of 10.000 per search.
2. "items": A list of the 10 most relevant results.
3. "aggregations": Aggregations of the results returned. Contains aggregations of:
    1. Genres
    2. Types
    3. Decades
4. "suggestions": Contains suggestions for the received query in case elasticsearch detects a misspelling and there are
   less than 30 results for the query.

#### Examples of responses

<details>
<summary>Response for "Iron Man" query</summary>

````json
{
  "total": 10000,
  "items": [
    {
      "id": "tt1300854",
      "title": "Iron Man 3",
      "original_title": "Iron Man Three",
      "genres": [
        "Action",
        "Adventure",
        "Sci-Fi"
      ],
      "type": "movie",
      "start_year": "2013-01-01",
      "average_rating": 7.1,
      "num_votes": 762766
    },
    {
      "id": "tt0371746",
      "title": "Iron Man",
      "original_title": "Iron Man",
      "genres": [
        "Action",
        "Adventure",
        "Sci-Fi"
      ],
      "type": "movie",
      "start_year": "2008-01-01",
      "average_rating": 7.9,
      "num_votes": 948386
    },
    {
      "id": "tt1228705",
      "title": "Iron Man 2",
      "original_title": "Iron Man 2",
      "genres": [
        "Action",
        "Adventure",
        "Sci-Fi"
      ],
      "type": "movie",
      "start_year": "2010-01-01",
      "average_rating": 7.0,
      "num_votes": 729802
    },
    {
      "id": "tt3296908",
      "title": "The Man with the Iron Heart",
      "original_title": "HHhH",
      "genres": [
        "Action",
        "Biography",
        "Thriller"
      ],
      "type": "movie",
      "start_year": "2017-01-01",
      "average_rating": 6.4,
      "num_votes": 14394
    },
    {
      "id": "tt1213641",
      "title": "First Man",
      "original_title": "First Man",
      "genres": [
        "Biography",
        "Drama",
        "History"
      ],
      "type": "movie",
      "start_year": "2018-01-01",
      "average_rating": 7.3,
      "num_votes": 171269
    },
    {
      "id": "tt1034314",
      "title": "Iron Sky",
      "original_title": "Iron Sky",
      "genres": [
        "Action",
        "Adventure",
        "Comedy"
      ],
      "type": "movie",
      "start_year": "2012-01-01",
      "average_rating": 5.9,
      "num_votes": 91326
    },
    {
      "id": "tt1258972",
      "title": "The Man with the Iron Fists",
      "original_title": "The Man with the Iron Fists",
      "genres": "Action",
      "type": "movie",
      "start_year": "2012-01-01",
      "average_rating": 5.4,
      "num_votes": 60673
    },
    {
      "id": "tt0478970",
      "title": "Ant-Man",
      "original_title": "Ant-Man",
      "genres": [
        "Action",
        "Adventure",
        "Comedy"
      ],
      "type": "movie",
      "start_year": "2015-01-01",
      "average_rating": 7.3,
      "num_votes": 577598
    },
    {
      "id": "tt3322310",
      "title": "Iron Fist",
      "original_title": "Iron Fist",
      "genres": [
        "Action",
        "Adventure",
        "Crime"
      ],
      "type": "tvSeries",
      "start_year": "2017-01-01",
      "end_year": "2018-01-01",
      "average_rating": 6.5,
      "num_votes": 115564
    },
    {
      "id": "tt2250912",
      "title": "Spider-Man: Homecoming",
      "original_title": "Spider-Man: Homecoming",
      "genres": [
        "Action",
        "Adventure",
        "Sci-Fi"
      ],
      "type": "movie",
      "start_year": "2017-01-01",
      "average_rating": 7.4,
      "num_votes": 521324
    }
  ],
  "aggregations": [
    {
      "genres": {
        "action": 2868,
        "adult": 768,
        "adventure": 2096,
        "animation": 2342,
        "biography": 770,
        "comedy": 9365,
        "crime": 2447,
        "documentary": 4362,
        "drama": 8352,
        "family": 1462,
        "fantasy": 1010,
        "film-noir": 25,
        "game-show": 217,
        "history": 634,
        "horror": 1285,
        "music": 1223,
        "musical": 188,
        "mystery": 1116,
        "news": 800,
        "reality-tv": 1430,
        "romance": 1085,
        "sci-fi": 1045,
        "short": 7795,
        "sport": 465,
        "talk-show": 2632,
        "thriller": 886,
        "war": 193,
        "western": 836
      }
    },
    {
      "types": {
        "movie": 5153,
        "short": 6465,
        "tvepisode": 17412,
        "tvminiseries": 197,
        "tvmovie": 995,
        "tvseries": 814,
        "tvshort": 70,
        "tvspecial": 91,
        "video": 2069,
        "videogame": 239
      }
    },
    {
      "decades": {
        "1880-1889": 4,
        "1890-1899": 26,
        "1900-1909": 213,
        "1910-1919": 984,
        "1921-1930": 351,
        "1930-1939": 329,
        "1941-1950": 244,
        "1950-1959": 1614,
        "1961-1970": 1940,
        "1971-1980": 1554,
        "1981-1990": 1466,
        "1991-2000": 2330,
        "2001-2010": 5302,
        "2011-2020": 15444,
        "2021-2030": 261
      }
    }
  ],
  "suggestions": {
    "title_term_suggestion": [
      {
        "text": "iron",
        "offset": 0,
        "length": 4,
        "options": []
      },
      {
        "text": "man",
        "offset": 5,
        "length": 3,
        "options": []
      }
    ]
  }
}
````

</details>


<details>
<summary>Suggestions part of the response for "abengers" query</summary>

````json
  {
  "suggestions": {
    "title_term_suggestion": [
      {
        "text": "abengers",
        "offset": 0,
        "length": 8,
        "options": [
          {
            "text": "avengers",
            "score": 0.009980146
          },
          {
            "text": "abangers",
            "score": 0.00075240026
          },
          {
            "text": "advengers",
            "score": 0.0007036785
          },
          {
            "text": "avengera",
            "score": 0.0007036785
          },
          {
            "text": "avengerus",
            "score": 0.0007036785
          }
        ]
      }
    ]
  }
````

</details>