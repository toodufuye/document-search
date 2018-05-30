package search.models.elasticResponse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregations.class)
@JsonDeserialize(as = ImmutableAggregations.class)
public abstract class Aggregations {
    public abstract Files files();
}
