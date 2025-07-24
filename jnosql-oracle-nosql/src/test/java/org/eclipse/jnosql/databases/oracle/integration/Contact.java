package org.eclipse.jnosql.databases.oracle.integration;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import org.eclipse.jnosql.databases.oracle.communication.ContactType;

@Entity
public record Contact(@Id String id, @Column String name, @Column ContactType type) {


}
