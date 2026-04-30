package at.jku.dke.task_app.hierarchical_clustering.dto;

public enum GenerationStrategyDto {
    COORDINATES, MATRIX;

    private final String translationKey;

    GenerationStrategyDto() {
        translationKey = "description." + this.toString().toLowerCase();
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
