package sacip.sti.dataentities;

import java.util.List;

public class Content {
    
    private String name;
    private String topic;
    private int difficulty;
    private String complexity;
    private boolean exercise;
    private String taxonomy;
    private List<String> tags;
    private String link;

    public Content() {
        super();
    }

    public Content(String name, String topic, int difficulty, String complexity, boolean exercise, String taxonomy, List<String> tags, String link) {
        this.name = name;
        this.topic = topic;
        this.difficulty = difficulty;
        this.complexity = complexity;
        this.exercise = exercise;
        this.taxonomy = taxonomy;
        this.tags = tags;
        this.link = link;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getComplexity() {
        return this.complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public boolean isExercise() {
        return this.exercise;
    }

    public boolean getExercise() {
        return this.exercise;
    }

    public void setExercise(boolean exercise) {
        this.exercise = exercise;
    }

    public String getTaxonomy() {
        return this.taxonomy;
    }

    public void setTaxonomy(String taxonomy) {
        this.taxonomy = taxonomy;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public String getTagsAsString() {

        StringBuilder builder = new StringBuilder();

        for (String tag : tags) {
            builder.append(tag+",");
        }
        
        if(!tags.isEmpty())
        builder.deleteCharAt(builder.lastIndexOf(","));

        return builder.toString();
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
