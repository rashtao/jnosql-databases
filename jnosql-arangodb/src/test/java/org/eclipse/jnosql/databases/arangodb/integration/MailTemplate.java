package org.eclipse.jnosql.databases.arangodb.integration;

import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public class MailTemplate {

    @Id
    private UUID id;

    private String to;

    private String from;

    private MailCategory category;
}
