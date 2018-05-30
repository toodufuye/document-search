package search.models.elasticResponse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBucket.class)
@JsonDeserialize(as = ImmutableBucket.class)
public abstract class Bucket {
    public abstract String key();
    public abstract int doc_count();

}
