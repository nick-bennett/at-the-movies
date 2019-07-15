package edu.cnm.deepdive.atthemovies.controller;

import edu.cnm.deepdive.atthemovies.model.dao.ActorRepository;
import edu.cnm.deepdive.atthemovies.model.dao.GenreRepository;
import edu.cnm.deepdive.atthemovies.model.dao.MovieRepository;
import edu.cnm.deepdive.atthemovies.model.entity.Actor;
import edu.cnm.deepdive.atthemovies.model.entity.Genre;
import edu.cnm.deepdive.atthemovies.model.entity.Movie;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("movies")
@ExposesResourceFor(Movie.class)
public class MovieController {

  private final MovieRepository repository;
  private final ActorRepository actorRepository;
  private final GenreRepository genreRepository;

  @Autowired
  public MovieController(MovieRepository repository, ActorRepository actorRepository,
      GenreRepository genreRepository) {
    this.repository = repository;
    this.actorRepository = actorRepository;
    this.genreRepository = genreRepository;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Movie> list(@RequestParam(value = "genre", required = false) UUID genreId) {
    if (genreId == null) {
      return repository.getAllByOrderByTitleAsc();
    } else {
      Genre genre = genreRepository.findById(genreId).get();
      return repository.getAllByGenreOrderByTitleAsc(genre);
    }
  }

  @GetMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Movie> search(@RequestParam(value = "q", required = true) String titleFragment) {
    return repository.getAllByTitleContainsOrderByTitleAsc(titleFragment);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Movie> post(@RequestBody Movie movie) {
    repository.save(movie);
    if (movie.getGenre() != null) {
      UUID genreId = movie.getGenre().getId();
      movie.setGenre(genreRepository.findById(genreId).get());
    }
    return ResponseEntity.created(movie.getHref()).body(movie);
  }

  @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Movie get(@PathVariable("id") UUID id) {
    return repository.findById(id).get();
  }

  @PutMapping(value = "{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Movie put(@PathVariable("id") UUID id, @RequestBody Movie movie) {
    Movie existingMovie = repository.findById(id).get();
    existingMovie.setGenre(movie.getGenre());
    existingMovie.setScreenwriter(movie.getScreenwriter());
    existingMovie.setTitle(movie.getTitle());
    repository.save(existingMovie);
    if (movie.getGenre() != null) {
      UUID genreId = movie.getGenre().getId();
      existingMovie.setGenre(genreRepository.findById(genreId).get());
    }
    return existingMovie;
  }

  @Transactional
  @DeleteMapping(value = "{id}")
  public void delete(@PathVariable("id") UUID id) {
    Movie movie = get(id);
    List<Actor> actors = movie.getActors();
    actors.forEach((actor) -> actor.getMovies().remove(movie));
    actorRepository.saveAll(actors);
    repository.delete(movie);
  }

  @PutMapping(value = "{movieId}/actors/{actorId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Movie attach(@PathVariable("movieId") UUID movieId, @PathVariable("actorId") UUID actorId) {
    Movie movie = get(movieId);
    Actor actor = actorRepository.findById(actorId).get();
    if (!actor.getMovies().contains(movie)) {
      actor.getMovies().add(movie);
    }
    actorRepository.save(actor);
    return movie;
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoSuchElementException.class)
  public void notFound() {}

}
