/*
 *   Copyright (c) 2025 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *    and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Otavio Santana
 */
package org.eclipse.jnosql.databases.arangodb.integration;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

@Entity
public class MailTemplate {

    @Id
    private UUID id;

    @Column
    private String to;

    @Column
    private String from;

    @Column
    private MailCategory category;

    @Column
    private boolean isDefault;

    public UUID getId() {
        return id;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public MailCategory getCategory() {
        return category;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MailTemplate that = (MailTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MailTemplate{" +
                "id=" + id +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", category=" + category +
                ", isDefault=" + isDefault +
                '}';
    }

    public static MailTemplateBuilder builder(){
        return new MailTemplateBuilder();
    }


    public static class MailTemplateBuilder{

        private String to;

        private String from;

        private MailCategory category;

        private boolean isDefault;

        public MailTemplateBuilder setTo(String to) {
            this.to = to;
            return this;
        }

        public MailTemplateBuilder setFrom(String from) {
            this.from = from;
            return this;
        }

        public MailTemplateBuilder setCategory(MailCategory category) {
            this.category = category;
            return this;
        }

        public MailTemplateBuilder setDefault(boolean aDefault) {
            isDefault = aDefault;
            return this;
        }

        public MailTemplate build(){
            MailTemplate mailTemplate = new MailTemplate();
            mailTemplate.id = UUID.randomUUID();
            mailTemplate.to = this.to;
            mailTemplate.from = this.from;
            mailTemplate.category = this.category;
            mailTemplate.isDefault = this.isDefault;
            return mailTemplate;
        }
    }
}
