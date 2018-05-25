package search;

import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreLabel;
import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import search.db.WordDao;
import search.models.ImmutableSearchResult;
import search.models.SearchResult;

import java.io.File;
import java.util.Random;

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
        Tokenizer tokenizer = ImmutableTokenizer.builder().file(testFile).jdbi(jdbi).build();
        // Setup the in memory database
        jdbi.useExtension(WordDao.class, dao -> {
                    dao.createTable();
                    tokenizer.tokens()
                            .forEach((List<CoreLabel> x) -> x.forEach(y -> dao.insert(y.originalText(), testFile.getAbsolutePath())));
                }
        );

        SearchResult stringResult = tokenizer.searchTokens("the", Arguments.Method.StringMatch)
                .orElse(ImmutableSearchResult.builder().fileName("ford_prefect").occurrences(9999).build());
        SearchResult regexResult = tokenizer.searchTokens("/the/", Arguments.Method.RegexMatch)
                .orElse(ImmutableSearchResult.builder().fileName("ford_prefect").occurrences(9999).build());
        SearchResult indexedResult = tokenizer.searchTokens("the", Arguments.Method.Indexed)
                .orElse(ImmutableSearchResult.builder().fileName("ford_prefect").occurrences(9999).build());

        assertTrue(stringResult.fileName().endsWith("hitchhikers.txt"));
        assertEquals(new Integer(21), stringResult.occurrences());
        assertEquals(stringResult, regexResult);
        assertEquals(stringResult, indexedResult);

        assertEquals(
                new Integer(29),
                tokenizer.searchTokens("/the|The/", Arguments.Method.RegexMatch)
                        .orElse(ImmutableSearchResult.builder().fileName("ford_prefect").occurrences(9999).build())
                        .occurrences());
    }

    @Test(expected = NullPointerException.class)
    public void searchTokensWithNullValue() {
        Tokenizer tokenizer = ImmutableTokenizer.builder().file(testFile).jdbi(jdbi).build();
        tokenizer.searchTokens("the", null);
    }

    @Test
    public void getCached() {
        File spyFile = spy(testFile);
        Tokenizer tokenizer = ImmutableTokenizer.builder().file(spyFile).jdbi(jdbi).build();
        Either<Exception, List<CoreLabel>> first = tokenizer.tokens();
        Either<Exception, List<CoreLabel>> second = tokenizer.tokens();
        verify(spyFile, times(1)).getPath();
        assertEquals(first, second);
    }

    @Test
    public void performanceTest() {
        // This test is only run if the following assumption passes.
        Assume.assumeTrue("true".equals(System.getProperty("system.performance.test")));

        // Database setup
        List<File> testFiles = List.of(
                new File(Resources.getResource("sample_files/sample_text/warp_drive.txt").getPath()),
                new File(Resources.getResource("sample_files/sample_text/hitchhikers.txt").getPath()),
                new File(Resources.getResource("sample_files/sample_text/french_armed_forces.txt").getPath())
        );
        List<Tokenizer> tokenizers = testFiles.map(x -> ImmutableTokenizer.builder().file(x).jdbi(jdbi).build());
        H2Utils.insertWordsIntoDatabase(jdbi, testFiles);

        Function1<Arguments.Method, Long> runPerformanceTest = (method) -> {
            // Todo: test for word "faster-than-light"
            List<String> words = List.of("France", "the", "and", "Hitchhiker", "XIV", "2004", "Improbability",
                    "a", "regime", "in", "I", "drive", "for", "often", "is", "has");
            Random random = new Random();
            int count = 0;
            Long startTime = System.currentTimeMillis();
            while (++count < 2000000) {
                tokenizers.forEach(x -> x.searchTokens(words.get(random.nextInt(words.size())), method));
            }
            return System.currentTimeMillis() - startTime;
        };

        System.out.println("\n<----------------  Performance Testing Results ---------------->");
        System.out.println(String.format("2,000,000 million searches with the String method took: %s ms", runPerformanceTest.apply(Arguments.Method.StringMatch)));
        System.out.println(String.format("2,000,000 million searches with the Regex method took: %s ms", runPerformanceTest.apply(Arguments.Method.RegexMatch)));
        System.out.println(String.format("2,000,000 million searches with the Indexed method took: %s ms", runPerformanceTest.apply(Arguments.Method.Indexed)));
    }
}