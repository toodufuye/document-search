import com.google.common.io.Resources;
import db.WordDao;
import edu.stanford.nlp.ling.CoreLabel;
import io.vavr.collection.List;
import io.vavr.control.Either;
import models.SearchResult;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TokenizerTest {
    private File testFile;
    private Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

    @Before
    public void setUp() {
        testFile = new File(Resources.getResource("sample_files/sample_text/hitchhikers.txt").getPath());
        jdbi.installPlugin(new SqlObjectPlugin());
    }

    @Test
    public void searchTokens() {
        Tokenizer tokenizer = Tokenizer.builder().file(testFile).jdbi(jdbi).build();
        // Setup the in memory database
        jdbi.useExtension(WordDao.class, dao -> {
                    dao.createTable();
                    tokenizer.getCached()
                            .forEach((List<CoreLabel> x) -> x.forEach(y -> dao.insert(y.originalText(), testFile.getAbsolutePath())));
                }
        );

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
        Tokenizer tokenizer = Tokenizer.builder().file(testFile).jdbi(jdbi).build();
        tokenizer.searchTokens("the", null);
    }

    @Test
    public void getCached() {
        File spyFile = spy(testFile);
        Tokenizer tokenizer = Tokenizer.builder().file(spyFile).jdbi(jdbi).build();
        Either<Exception, List<CoreLabel>> first = tokenizer.getCached();
        Either<Exception, List<CoreLabel>> second = tokenizer.getCached();
        verify(spyFile, times(1)).getPath();
        assertEquals(first, second);
    }
}