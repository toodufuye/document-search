package search.models.elasticResponse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableShards.class)
@JsonDeserialize(as = ImmutableShards.class)
public abstract class Shards {
    public abstract int total();
    public abstract int successful();
    public abstract int skipped();
    public abstract int failed();
}
