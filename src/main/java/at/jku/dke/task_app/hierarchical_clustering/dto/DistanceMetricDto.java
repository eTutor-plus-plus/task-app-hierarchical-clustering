package at.jku.dke.task_app.hierarchical_clustering.dto;

public enum DistanceMetricDto {
    EUCLIDEAN, MANHATTAN;

    private final String translationKey;

    DistanceMetricDto() {
        translationKey = "description." + this.toString().toLowerCase();
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
