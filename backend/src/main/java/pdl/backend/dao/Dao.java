package pdl.backend.dao;

import java.util.Optional;
import java.util.List;

// Generic DAO (Data Access Object) interface defining standard CRUD operations
public interface Dao<T> {

    // Persists a new entity to the data store
    void create(final T t);

    // Retrieves a single entity by its ID, returns empty if not found
    Optional<T> retrieve(final long id);

    // Retrieves all entities of this type from the data store
    List<T> retrieveAll();

    // Updates an existing entity using the provided parameters
    void update(final T t, final String[] params);

    // Deletes the given entity from the data store
    void delete(final T t);
}