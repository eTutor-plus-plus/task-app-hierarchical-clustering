package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram.DendrogramModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DendrogramModelConverter implements AttributeConverter<DendrogramModel, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(DendrogramModel model) {
        if (model == null) return null;
        try {
            return MAPPER.writeValueAsString(model);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize DendrogramModel to JSON", e);
        }
    }

    @Override
    public DendrogramModel convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, DendrogramModel.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize DendrogramModel from JSON", e);
        }
    }
}
