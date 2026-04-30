package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.dto.DistanceMetricDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class DistanceMetricConverter implements AttributeConverter<DistanceMetricDto, String> {

    public DistanceMetricConverter() {}

    @Override
    public String convertToDatabaseColumn(DistanceMetricDto distanceMetricDto) {
        if (distanceMetricDto == null)
            return null;
        return distanceMetricDto.name().toLowerCase();
    }

    @Override
    public DistanceMetricDto convertToEntityAttribute(String s) {
        if (s == null)
            return null;

        return Stream.of(DistanceMetricDto.values())
            .filter(g -> g.name().toLowerCase().equals(s))
            .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
