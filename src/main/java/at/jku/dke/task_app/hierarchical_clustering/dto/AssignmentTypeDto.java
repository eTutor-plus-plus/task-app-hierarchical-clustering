package at.jku.dke.task_app.hierarchical_clustering.dto;

public enum AssignmentTypeDto {
    COORDINATES, MATRIX;

    private final String translationKey;

    AssignmentTypeDto() {
        translationKey = "description." + this.toString().toLowerCase();
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
