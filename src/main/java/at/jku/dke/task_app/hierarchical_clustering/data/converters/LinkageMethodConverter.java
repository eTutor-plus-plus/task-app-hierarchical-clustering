package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

/**
 * Converts the database {@code linkage_method} enum to the java {@link LinkageMethod} enum and to/from string.
 */
@Converter
public class LinkageMethodConverter implements AttributeConverter<LinkageMethod, String> {

    /**
     * Creates a new instance of class {@linkplain LinkageMethodConverter}.
     */
    public LinkageMethodConverter() {}

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
