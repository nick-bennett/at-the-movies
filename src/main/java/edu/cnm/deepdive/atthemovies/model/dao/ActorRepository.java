package edu.cnm.deepdive.atthemovies.model.dao;

import edu.cnm.deepdive.atthemovies.model.entity.Actor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ActorRepository extends CrudRepository<Actor, UUID> {

  // TODO Declare any required query methods.

  List<Actor> getAllByOrderByName();

}
