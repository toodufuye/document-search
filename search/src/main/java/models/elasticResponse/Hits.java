package models.elasticResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;

@Value
@Builder
@JsonDeserialize(builder = Hits.HitsBuilder.class)
public class Hits {
    private int total;
    private Double max_score;
    private ArrayList<String> hits;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class HitsBuilder {

    }
}
