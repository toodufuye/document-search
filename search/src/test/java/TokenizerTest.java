import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreLabel;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
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
    public void getCached() {
        File spyFile = spy(testFile);
        Tokenizer tokenizer = Tokenizer.builder().file(spyFile).build();
        Either<Exception, List<CoreLabel>> first = tokenizer.getCached();
        Either<Exception, List<CoreLabel>> second = tokenizer.getCached();
        verify(spyFile, times(1)).getPath();
        assertEquals(first, second);
    }

//    @Test
//    public void performanceTest() {
//        // This test is only run if the following assumption passes.
//        Assume.assumeTrue("true".equals(System.getProperty("system.performance.test")));
//
//        // Database setup
//        List<File> testFiles = List.of(
//                new File(Resources.getResource("sample_files/sample_text/warp_drive.txt").getPath()),
//                new File(Resources.getResource("sample_files/sample_text/hitchhikers.txt").getPath()),
//                new File(Resources.getResource("sample_files/sample_text/french_armed_forces.txt").getPath())
//        );
//        List<Tokenizer> tokenizers = testFiles.map(x -> Tokenizer.builder().file(x).jdbi(jdbi).build());
//        H2Utils.insertWordsIntoDatabase(jdbi, testFiles);
//
//        Function1<Method, Long> runPerformanceTest = (method) -> {
//            // Todo: test for word "faster-than-light"
//            List<String> words = List.of("France", "the", "and", "Hitchhiker", "XIV", "2004", "Improbability",
//                    "a", "regime", "in", "I", "drive", "for", "often", "is", "has");
//            Random random = new Random();
//            int count = 0;
//            Long startTime = System.currentTimeMillis();
//            while (++count < 2000000) {
//                tokenizers.forEach(x -> x.searchTokens(words.get(random.nextInt(words.size())), method));
//            }
//            return System.currentTimeMillis() - startTime;
//        };
//
//        System.out.println("\n<----------------  Performance Testing Results ---------------->");
//        System.out.println(String.format("2,000,000 million searches with the String method took: %s ms", runPerformanceTest.apply(Method.StringMatch)));
//        System.out.println(String.format("2,000,000 million searches with the Regex method took: %s ms", runPerformanceTest.apply(Method.RegexMatch)));
//        System.out.println(String.format("2,000,000 million searches with the Indexed method took: %s ms", runPerformanceTest.apply(Method.Indexed)));
//    }
}