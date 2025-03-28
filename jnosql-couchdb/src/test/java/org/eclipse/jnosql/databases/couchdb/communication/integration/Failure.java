package org.eclipse.jnosql.databases.couchdb.communication.integration;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity
public record Failure(
        @Id String id,
        @Column byte[] data) {

}