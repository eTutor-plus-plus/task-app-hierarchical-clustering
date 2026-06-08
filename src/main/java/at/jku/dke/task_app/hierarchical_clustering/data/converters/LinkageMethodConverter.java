package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class LinkageMethodConverter implements AttributeConverter<LinkageMethod, String> {
    @Override
    public String convertToDatabaseColumn(LinkageMethod linkageMethod) {
        if (linkageMethod == null)
            return null;
        return linkageMethod.name().toLowerCase();
    }

    @Override
    public LinkageMethod convertToEntityAttribute(String s) {
        if (s == null)
            return null;
        return Stream.of(LinkageMethod.values())
                    .filter(g -> g.name().toLowerCase().equals(s))
                    .findAny().orElseThrow(IllegalArgumentException::new);
    }
}
