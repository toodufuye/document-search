package models;

import lombok.Value;

@Value public class SearchResult {
    String fileName;
    Integer occurrences;
}
