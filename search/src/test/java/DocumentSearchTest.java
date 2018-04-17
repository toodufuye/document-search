import com.google.common.io.Resources;
import net.jodah.failsafe.RetryPolicy;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DocumentSearchTest {
    private RetryPolicy retryPolicy = new RetryPolicy()
            .retryOn(NumberFormatException.class)
            .withDelay(1, TimeUnit.MILLISECONDS)
            .withMaxRetries(1);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void searchWithBadDirectory() {
        InputStream is = new ByteArrayInputStream("the\n1".getBytes(StandardCharsets.UTF_8));
        DocumentSearch documentSearch = new DocumentSearch(retryPolicy, "vogon_poetry", is, System.out);
        assertTrue(documentSearch.Search().contains("There are no text files in the directory vogon_poetry"));
    }

    @Test
    public void searchWithNoInput() {
        InputStream is = new ByteArrayInputStream("\n\n1".getBytes(StandardCharsets.UTF_8));
        DocumentSearch documentSearch = new DocumentSearch(
                retryPolicy,
                Resources.getResource("sample_files").getPath(),
                is,
                new PrintStream(new ByteArrayOutputStream())); // I'm suppressing console output intentionally
        assertTrue(documentSearch.Search().contains("Please provide a non empty string for the search term"));
    }

    @Test
    public void searchWithNoMethod() {
        InputStream is = new ByteArrayInputStream("the\na".getBytes(StandardCharsets.UTF_8));
        DocumentSearch documentSearch = new DocumentSearch(
                retryPolicy,
                Resources.getResource("sample_files").getPath(),
                is,
                new PrintStream(new ByteArrayOutputStream()));
        assertTrue(documentSearch.Search().contains("Please provide a valid search method"));
    }
}