package search.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDocument.class)
@JsonDeserialize(as = ImmutableDocument.class)
public abstract class Document {
    public abstract String fileName();
    public abstract String content();
}
