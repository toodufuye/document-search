package models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Arguments {
    private String input;
    private Method method;
}
