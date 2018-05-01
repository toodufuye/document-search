package models.elasticResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = AggResponse.AggResponseBuilder.class)
public class AggResponse {
    private int took;
    private boolean timed_out;
    private Shards _shards;
    private Hits hits;
    private Aggregations aggregations;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class AggResponseBuilder {

    }
}
