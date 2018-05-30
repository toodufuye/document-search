package search;

import io.vavr.collection.List;
import io.vavr.control.Try;
import org.junit.Test;
import search.models.ImmutableSearchResult;
import search.models.SearchResult;
import static org.junit.Assert.*;

public class ImmutablesAndTry {
    @Test
    public void random() {
        SearchResult result = ImmutableSearchResult.builder().fileName("file").occurrences(1).build();
        assertEquals("file", result.fileName());
    }

    @Test
    public void listOfStuff() {
        List<SearchResult> xs = List.of(ImmutableSearchResult.builder().fileName("one").occurrences(1).build());
        assertEquals(1, xs.size());
    }

    @Test
    public void tryList() {
        Try<List<SearchResult>> xs = Try.of(() -> List.of(ImmutableSearchResult.builder().fileName("one").occurrences(1).build()));
        List<SearchResult> ss = xs.getOrElse(List::empty);
        assertEquals(1, ss.size());
    }

    @Test
    public void tryListOneVariable() {
        List<SearchResult> xs = Try.of(() -> List.<SearchResult>of(ImmutableSearchResult.builder().fileName("one").occurrences(2).build())).getOrElse(List.empty());
        assertEquals(1, xs.size());
    }
}
