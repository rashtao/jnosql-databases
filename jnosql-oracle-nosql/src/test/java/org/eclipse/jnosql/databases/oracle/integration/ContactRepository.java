package org.eclipse.jnosql.databases.oracle.integration;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.databases.oracle.communication.ContactType;

import java.util.List;

@Repository
public interface ContactRepository extends BasicRepository<Contact, String> {

    @Query("where type = :type")
    List<Contact> findByType(@Param("type") ContactType type);
}
