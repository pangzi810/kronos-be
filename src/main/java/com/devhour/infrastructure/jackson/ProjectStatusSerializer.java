package com.devhour.infrastructure.jackson;

import com.devhour.domain.model.valueobject.ProjectStatus;
import com.fasterxml.jackson.databind.JsonSerializer;

public class ProjectStatusSerializer extends JsonSerializer<ProjectStatus> {
    @Override
    public void serialize(ProjectStatus value, com.fasterxml.jackson.core.JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) 
            throws java.io.IOException {
        gen.writeString(value.value());
    }
}
