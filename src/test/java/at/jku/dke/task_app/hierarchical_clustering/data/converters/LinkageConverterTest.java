package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkageConverterTest {

    private final LinkageMethodConverter converter = new LinkageMethodConverter();

    @Test
    void testConvertToDatabaseColumn() {
        // Arrange & Act & Assert
        assertEquals("single", converter.convertToDatabaseColumn(LinkageMethod.SINGLE));
        assertEquals("complete", converter.convertToDatabaseColumn(LinkageMethod.COMPLETE));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute() {
        // Arrange & Act & Assert
        assertEquals(LinkageMethod.SINGLE, converter.convertToEntityAttribute("single"));
        assertEquals(LinkageMethod.COMPLETE, converter.convertToEntityAttribute("complete"));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testInvalidValues() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("SINGLE"));
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(""));
    }
}
