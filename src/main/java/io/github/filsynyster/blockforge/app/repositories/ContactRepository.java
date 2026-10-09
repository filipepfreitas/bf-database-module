package io.github.filsynyster.blockforge.app.repositories;

import io.github.filsynyster.blockforge.database.annotations.Query;
import io.github.filsynyster.blockforge.database.core.repository.DataRepository;
import io.github.filsynyster.blockforge.app.models.Contact;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface ContactRepository extends DataRepository<Contact, Long> {

    Contact findByName(String name);

    Contact findByIdNative(Long id);

    List<Contact> findAllNative();

    List<Contact> findByAge();

    List<Contact> findByAgeGreaterThan(int age);

    Set<Contact> findAllAsSet();

    @Query("SELECT * FROM contacts WHERE age > 10")
    Set<Contact> findAdultsAsSet();

    @Query("SELECT * FROM contacts")
    Collection<Contact> findAllAsCollection();

    @Query("SELECT * FROM contacts WHERE age > 10")
    Collection<Contact> findByAgeAsCollection();

    @Query("SELECT * FROM contacts WHERE name = ?")
    Optional<Contact> findOptionalByName(String name);

    @Query("SELECT * FROM contacts WHERE id = ?")
    Optional<Contact> findOptionalById(Long id);

    @Query("SELECT * FROM contacts")
    Stream<Contact> findAllAsStream();

    @Query("SELECT * FROM contacts WHERE age > 10")
    Stream<Contact> findByAgeAsStream();
}