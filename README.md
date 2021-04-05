# Search path project

This is a demo of a Micronaut API Client which manage petitions to an elasticsearch client done as part of my internship
at Empathy.co

## Before the start

Make sure you have installed in your system the following items:

* Java 11
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

* This kind of search looks specifically for the fields passed by parameters. Gives better results than the general
  query if you know the values and type of the elements you're passing by parameters.
* The fields can be combined
* The fields can be the following:
    * title
    * type
    * genre
        * one or more values separated by commas
    * date
        * one or more range of years separated by commas

##### Examples of search by query + fields:

Films titled "Spiderman 3"
> /search?query=Spiderman+3&type=movie

Media titled "The great adventure" of genre drama and adventure
> /search?query=The+great+adventure&genre=drama,adventure

Film titled "Call me by your name" of genre drama and romance released between 2010 and 2020
> /search?query=Call+me+by+your+name&type=movie&genre=drama,romance&date=2010-2020

Film of genre drama released between 2010 and 2013, and between 1980 and 1995
> /search?query=The+Schindler's+list&genre=drama&date=2010-2013,1980-1995