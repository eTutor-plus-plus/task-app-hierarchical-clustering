package at.jku.dke.task_app.hierarchical_clustering.dto;

public enum LinkageMethodDto {
    SINGLE, COMPLETE;

    private final String translationKey;

    LinkageMethodDto() {
        translationKey = "description." + this.toString().toLowerCase();
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
