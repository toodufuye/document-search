package models.elasticResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;

@Value
@Builder
@JsonDeserialize(builder = Files.FilesBuilder.class)
public class Files {
    private int doc_count_error_upper_bound;
    private int sum_other_doc_count;
    private ArrayList<Bucket> buckets;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class FilesBuilder {

    }
}
