package com.devhour.infrastructure.jackson;

import com.devhour.domain.model.valueobject.ProjectStatus;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ProjectStatusDeserializer extends JsonDeserializer<ProjectStatus>{
    @Override
    public ProjectStatus deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) 
            throws com.fasterxml.jackson.core.JsonProcessingException, java.io.IOException {
        String statusValue = p.getText();
        return new ProjectStatus(statusValue);
    }
}
