package search.models.elasticResponse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggResponse.class)
@JsonDeserialize(as = ImmutableAggResponse.class)
public abstract class AggResponse {
    public abstract int took();
    public abstract boolean timed_out();
    public abstract Shards _shards();
    public abstract Hits hits();
    public abstract Aggregations aggregations();

}
