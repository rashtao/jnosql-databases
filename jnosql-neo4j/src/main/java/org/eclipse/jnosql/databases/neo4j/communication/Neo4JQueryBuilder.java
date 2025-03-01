/*
 *
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.eclipse.jnosql.databases.neo4j.communication;


import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum Neo4JQueryBuilder {
    INSTANCE;

    public String buildQuery(DeleteQuery query, Map<String, Object> parameters) {
        StringBuilder cypher = new StringBuilder("MATCH (e:");
        cypher.append(query.name()).append(")");

        query.condition().ifPresent(c -> {
            cypher.append(" WHERE ");
            createWhereClause(cypher, c, parameters);
        });

        List<String> columns = query.columns();
        if (!columns.isEmpty()) {
            cypher.append(" SET ");
            cypher.append(columns.stream().map(col -> "e." + col + " = NULL").collect(Collectors.joining(", ")));
        } else {
            cypher.append(" DELETE e");
        }

        return cypher.toString();
    }

    public String buildQuery(SelectQuery query, Map<String, Object> parameters) {
        StringBuilder cypher = new StringBuilder("MATCH (e:");
        cypher.append(query.name()).append(")");

        query.condition().ifPresent(c -> {
            cypher.append(" WHERE ");
            createWhereClause(cypher, c, parameters);
        });

        query.sorts().forEach(sort -> cypher.append(" ORDER BY e.").append(sort.property()).append(" ")
                .append(sort.isAscending() ? "ASC" : "DESC"));

        if (query.skip() > 0) {
            cypher.append(" SKIP ").append(query.skip());
        }

        if (query.limit() > 0) {
            cypher.append(" LIMIT ").append(query.limit());
        }

        cypher.append(" RETURN ");
        List<String> columns = query.columns();
        if (columns.isEmpty()) {
            cypher.append("e");
        } else {
            cypher.append(columns.stream().map(col -> "e." + col).collect(Collectors.joining(", ")));
        }

        return cypher.toString();
    }

    private void createWhereClause(StringBuilder cypher, CriteriaCondition condition, Map<String, Object> parameters) {
        Element element = condition.element();
        String fieldName = element.name();

        switch (condition.condition()) {
            case EQUALS:
            case GREATER_THAN:
            case GREATER_EQUALS_THAN:
            case LESSER_THAN:
            case LESSER_EQUALS_THAN:
            case LIKE:
            case IN:
                parameters.put(fieldName, element.get());
                cypher.append("e.").append(fieldName).append(" ")
                        .append(getConditionOperator(condition.condition()))
                        .append(" $").append(fieldName);
                break;
            case NOT:
                cypher.append("NOT (");
                createWhereClause(cypher, element.get(CriteriaCondition.class), parameters);
                cypher.append(")");
                break;
            case AND:
            case OR:
                cypher.append("(");
                List<CriteriaCondition> conditions = element.get(new TypeReference<>() {});
                for (int index = 0; index < conditions.size(); index++) {
                    if (index > 0) {
                        cypher.append(" ").append(getConditionOperator(condition.condition())).append(" ");
                    }
                    createWhereClause(cypher, conditions.get(index), parameters);
                }
                cypher.append(")");
                break;
            default:
                throw new CommunicationException("Unsupported condition: " + condition.condition());
        }
    }

    private String getConditionOperator(Condition condition) {
        return switch (condition) {
            case EQUALS -> "=";
            case GREATER_THAN -> ">";
            case GREATER_EQUALS_THAN -> ">=";
            case LESSER_THAN -> "<";
            case LESSER_EQUALS_THAN -> "<=";
            case LIKE -> "CONTAINS";
            case IN -> "IN";
            case AND -> "AND";
            case OR -> "OR";
            default -> throw new CommunicationException("Unsupported operator: " + condition);
        };
    }
}