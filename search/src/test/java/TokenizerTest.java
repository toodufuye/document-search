import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreLabel;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TokenizerTest {
    private File testFile;

    @Before
    public void setUp() {
        testFile = new File(Resources.getResource("sample_files/sample_text/hitchhikers.txt").getPath());
    }

    @Test
    public void searchTokens() {
        Tokenizer tokenizer = new Tokenizer(testFile);
        SearchResult stringResult = tokenizer.searchTokens("the", Arguments.Method.StringMatch)
                .orElse(new SearchResult("ford_prefect", 9999));
        SearchResult regexResult = tokenizer.searchTokens("/the/", Arguments.Method.RegexMatch)
                .orElse(new SearchResult("ford_prefect", 9999));
        SearchResult indexedResult = tokenizer.searchTokens("the", Arguments.Method.Indexed)
                .orElse(new SearchResult("ford_prefect", 9999));

        assertTrue(stringResult.getFileName().endsWith("hitchhikers.txt"));
        assertEquals(new Integer(21), stringResult.getOccurrences());
        assertEquals(stringResult, regexResult);
        assertEquals(stringResult, indexedResult);

        assertEquals(
                new Integer(29),
                tokenizer.searchTokens("/the|The/", Arguments.Method.RegexMatch)
                        .orElse(new SearchResult("ford_prefect", 9999))
                        .getOccurrences());
    }

    @Test(expected = NullPointerException.class)
    public void searchTokensWithNullValue() {
        Tokenizer tokenizer = new Tokenizer(testFile);
        tokenizer.searchTokens("the", null);
    }

    @Test
    public void getCached() {
        File spyFile = spy(testFile);
        Tokenizer tokenizer = new Tokenizer(spyFile);
        Either<Exception, List<CoreLabel>> first = tokenizer.getCached();
        Either<Exception, List<CoreLabel>> second = tokenizer.getCached();
        verify(spyFile, times(1)).getPath();
        assertEquals(first, second);
    }
}