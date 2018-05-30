package search.models.elasticResponse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.ArrayList;

@Value.Immutable
@JsonSerialize(as = ImmutableHits.class)
@JsonDeserialize(as = ImmutableHits.class)
public abstract class Hits {
    public abstract int total();
    public abstract Double max_score();
    public abstract ArrayList<String> hits();
}
