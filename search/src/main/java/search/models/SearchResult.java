package search.models;

import org.immutables.value.Value;

@Value.Immutable
public abstract class SearchResult {
    public abstract String fileName();
    public abstract Integer occurrences();
}
