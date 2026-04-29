package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.dto.LinkageMethodDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class LinkageMethodConverter implements AttributeConverter<LinkageMethodDto, String> {
    @Override
    public String convertToDatabaseColumn(LinkageMethodDto linkageMethodDto) {
        if (linkageMethodDto == null)
            return null;
        return linkageMethodDto.name().toLowerCase();
    }

    @Override
    public LinkageMethodDto convertToEntityAttribute(String s) {
        if (s == null)
            return null;
        return Stream.of(LinkageMethodDto.values())
                    .filter(g -> g.name().toLowerCase().equals(s))
                    .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
