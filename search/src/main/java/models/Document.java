package models;

import lombok.Value;

@Value
public class Document {
    public String fileName;
    public String content;
}
