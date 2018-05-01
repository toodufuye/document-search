import com.google.common.io.Resources;
import net.jodah.failsafe.RetryPolicy;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class DocumentSearchTest {
    private RetryPolicy retryPolicy = new RetryPolicy()
            .retryOn(NumberFormatException.class)
            .withDelay(1, TimeUnit.MILLISECONDS)
            .withMaxRetries(1);

    private Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test");

    @Before
    public void setUp() {
        jdbi.installPlugin(new SqlObjectPlugin());
    }

    @Test
    public void searchWithBadDirectory() {
        InputStream is = new ByteArrayInputStream("the\n1".getBytes(StandardCharsets.UTF_8));
        DocumentSearch documentSearch = DocumentSearch.builder()
                .retryPolicy(retryPolicy)
                .directory("vogon_poetry")
                .in(is)
                .out(System.out)
                .jdbi(jdbi)
                .build();
        assertTrue(documentSearch.Search().contains("There are no text files in the directory vogon_poetry"));
    }

    @Test
    public void searchWithNoInput() {
        InputStream is = new ByteArrayInputStream("\n\n1".getBytes(StandardCharsets.UTF_8));
        DocumentSearch documentSearch = DocumentSearch.builder()
                .retryPolicy(retryPolicy)
                .directory(Resources.getResource("sample_files").getPath())
                .in(is)
                .out(new PrintStream(new ByteArrayOutputStream())) // I'm suppressing console output intentionally
                .jdbi(jdbi)
                .build();
        assertTrue(documentSearch.Search().contains("Please provide a non empty string for the search term"));
    }

    @Test
    public void searchWithNoMethod() {
        InputStream is = new ByteArrayInputStream("the\na".getBytes(StandardCharsets.UTF_8));
        DocumentSearch documentSearch = DocumentSearch.builder()
                .retryPolicy(retryPolicy)
                .directory(Resources.getResource("sample_files").getPath())
                .in(is)
                .out(new PrintStream(new ByteArrayOutputStream())) // I'm suppressing console output intentionally
                .jdbi(jdbi)
                .build();
        assertTrue(documentSearch.Search().contains("Elasped time"));
    }
}