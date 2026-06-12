package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

/**
 * Converts the database {@code distance_metric} enum to the java {@link DistanceMetric} enum and to/from string.
 */
@Converter
public class DistanceMetricConverter implements AttributeConverter<DistanceMetric, String> {

    /**
     * Creates a new instance of class {@linkplain DistanceMetricConverter}.
     */
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
