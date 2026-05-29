package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.dto.DistanceMetric;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class DistanceMetricConverter implements AttributeConverter<DistanceMetric, String> {

    public DistanceMetricConverter() {}

    @Override
    public String convertToDatabaseColumn(DistanceMetric distanceMetric) {
        if (distanceMetric == null)
            return null;
        return distanceMetric.name().toLowerCase();
    }

    @Override
    public DistanceMetric convertToEntityAttribute(String s) {
        if (s == null)
            return null;

        return Stream.of(DistanceMetric.values())
            .filter(g -> g.name().toLowerCase().equals(s))
            .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
