package models;

import lombok.Builder;
import lombok.Value;

@Value
public class DocumentToken {
    public String fileName;
    public String token;
}
