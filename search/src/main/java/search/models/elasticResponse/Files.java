package search.models.elasticResponse;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.ArrayList;

@Value.Immutable
@JsonSerialize(as = ImmutableFiles.class)
@JsonDeserialize(as = ImmutableFiles.class)
public abstract class Files {
    public abstract int doc_count_error_upper_bound();
    public abstract int sum_other_doc_count();
    public abstract ArrayList<Bucket> buckets();
}
