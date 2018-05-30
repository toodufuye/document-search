package search.models;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Arguments {
    public abstract String input();
    public abstract Method method();
}
