package search;

import net.jodah.failsafe.RetryPolicy;
import org.junit.Before;
import org.junit.Test;
import search.models.Arguments;
import search.models.Method;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;


public class DocumentsGetSearchMethodTest {
    private RetryPolicy retryPolicy;

    @Before
    public void setUp() {
        retryPolicy = new RetryPolicy()
                .retryOn(NumberFormatException.class)
                .withDelay(1, TimeUnit.MILLISECONDS)
                .withMaxRetries(1);
    }

    @Test
    public void getSearchMethodHappyPath() {
        InputStream is = new ByteArrayInputStream("the\n1".getBytes(StandardCharsets.UTF_8));
        Arguments result = DocumentSearch.getSearchMethod(is, System.out, retryPolicy);
        assertEquals("the", result.input());
        assertEquals(result.method(), Method.StringMatch);
    }

    @Test
    public void getSearchMethodRetries() {
        InputStream is = new ByteArrayInputStream("\n\n".getBytes(StandardCharsets.UTF_8));
        Arguments result = DocumentSearch.getSearchMethod(is, System.out, retryPolicy);
        assertTrue(result.input().isEmpty());
        assertEquals(result.method(), Method.StringMatch);
    }

    @Test
    public void getSearchRetriesThenPasses() {
        InputStream is = new ByteArrayInputStream("\nthe\n1".getBytes(StandardCharsets.UTF_8));
        Arguments result = DocumentSearch.getSearchMethod(is, System.out, retryPolicy);
        assertFalse(result.input().isEmpty());
        assertEquals(result.method(), Method.StringMatch);
    }

    @Test
    public void getSearchMethodRegex() {
        InputStream is = new ByteArrayInputStream("the\n2".getBytes(StandardCharsets.UTF_8));
        Arguments result = DocumentSearch.getSearchMethod(is, System.out, retryPolicy);
        assertEquals(result.method(), Method.RegexMatch);
    }

    @Test
    public void getSearchMethodIndexed() {
        InputStream is = new ByteArrayInputStream("the\n3".getBytes(StandardCharsets.UTF_8));
        Arguments result = DocumentSearch.getSearchMethod(is, System.out, retryPolicy);
        assertEquals(result.method(), Method.Indexed);
    }

    @Test
    public void getSearchMethodEmpty() {
        InputStream is = new ByteArrayInputStream("the\n\n4".getBytes(StandardCharsets.UTF_8));
        Arguments result = DocumentSearch.getSearchMethod(is, System.out, retryPolicy);
        assertEquals(result.method(), Method.StringMatch);
    }

    @Test
    public void getSearchIOException() throws IOException {
        InputStream mockInputStream = spy(new ByteArrayInputStream("the\n3".getBytes(StandardCharsets.UTF_8)));
        doThrow(new IOException()).when(mockInputStream).close();
        Arguments result = DocumentSearch.getSearchMethod(mockInputStream, System.out, retryPolicy);
        assertEquals(result.method(), Method.StringMatch);
    }
}