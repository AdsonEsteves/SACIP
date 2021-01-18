package sacip.sti.dataentities;

import java.util.List;

public class Content{
    
    private String name;
    private String descricao;
    private String topic;
    private String complexity;
    private boolean exercise;
    private int taxonomy;
    private List<String> tags;
    private String link;
    private int level;
    private String imageLink;

    public int pontos = 0;

    public Content() {
        super();
    }


    public Content(String name, String descricao, int level, String topic, String complexity, boolean exercise, int taxonomy, List<String> tags, String link, String imageLink) {
        this.name = name;
        this.descricao = descricao;
        this.topic = topic;
        this.complexity = complexity;
        this.exercise = exercise;
        this.taxonomy = taxonomy;
        this.tags = tags;
        this.link = link;
        this.level = level;
        this.imageLink = imageLink;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public int getTaxonomy() {
        return this.taxonomy;
    }

    public void setTaxonomy(int taxonomy) {
        this.taxonomy = taxonomy;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public String buildTagsAsString() {

        StringBuilder builder = new StringBuilder();

        for (String tag : tags) {
            builder.append("'"+tag+"',");
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

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    @Override
    public String toString() {
        return "{" +
            " name='" + getName() + "'" +
            ", descricao='" + getDescricao() + "'" +
            ", topic='" + getTopic() + "'" +
            ", complexity='" + getComplexity() + "'" +
            ", exercise='" + isExercise() + "'" +
            ", taxonomy='" + getTaxonomy() + "'" +
            ", tags:[" + buildTagsAsString() + "]" +
            ", link='" + getLink() + "'" +
            ", level='" + getLevel() + "'" +
            ", imageLink='" + getImageLink() + "'" +
            "}";
    }
    

}
