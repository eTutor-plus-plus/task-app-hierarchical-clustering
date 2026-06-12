package at.jku.dke.task_app.hierarchical_clustering.data.converters;

import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceMetricConverterTest {

    private final DistanceMetricConverter converter = new DistanceMetricConverter();

    @Test
    void testConvertToDatabaseColumn() {
        // Arrange & Act & Assert
        assertEquals("euclidean", converter.convertToDatabaseColumn(DistanceMetric.EUCLIDEAN));
        assertEquals("manhattan", converter.convertToDatabaseColumn(DistanceMetric.MANHATTAN));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute() {
        // Arrange & Act & Assert
        assertEquals(DistanceMetric.EUCLIDEAN, converter.convertToEntityAttribute("euclidean"));
        assertEquals(DistanceMetric.MANHATTAN, converter.convertToEntityAttribute("manhattan"));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testInvalidValues() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("EUCLIDEAN"));
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(""));
    }
}
