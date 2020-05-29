package io.confluent.testcontainers.support;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import io.confluent.developer.Movie;

public interface Parser {

  static List<String> parseArray(String text) {
    return Collections.list(new StringTokenizer(text, "|")).stream()
        .map(token -> (String) token)
        .collect(Collectors.toList());
  }

  static Movie parseMovie(String text) {
    Movie movie = new Movie();
    String[] tokens = text.split("::");
    String id = tokens[0];
    movie.setMovieId(Long.parseLong(id));

    String title = tokens[1];
    movie.setTitle(title);

    String releaseYear = tokens[2];
    movie.setReleaseYear(Integer.parseInt(releaseYear));

    String country = tokens[4];
    movie.setCountry(country);

    //String rating = tokens[5];
    //movie.setRating(Float.parseFloat(rating));

    String genres = tokens[7];
    movie.setGenres(Parser.parseArray(genres));

    String actors = tokens[8];
    movie.setActors(Parser.parseArray(actors));

    String directors = tokens[9];
    movie.setDirectors(Parser.parseArray(directors));
    String composers = tokens[10];
    movie.setComposers(Parser.parseArray(composers));

    String screenwriters = tokens[11];
    movie.setScreenwriters(Parser.parseArray(screenwriters));

    String cinematographer = tokens[12];
    movie.setCinematographer(cinematographer);

    String productionCompanies = "";
    if (tokens.length > 13) {
      productionCompanies = tokens[13];
    }
    movie.setProductionCompanies(Parser.parseArray(productionCompanies));

    return movie;
  }


}
