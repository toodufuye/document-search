import lombok.val;
import net.jodah.failsafe.RetryPolicy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ArgumentsTest {
    private RetryPolicy retryPolicy;

    @Before
    public void setUp() {
        retryPolicy = new RetryPolicy()
                .retryOn(NumberFormatException.class)
                .withDelay(1, TimeUnit.SECONDS)
                .withMaxRetries(1);
    }

    @Test
    public void getSearchMethodHappyPath() {
        InputStream is = new ByteArrayInputStream("the\n1".getBytes(StandardCharsets.UTF_8));
        val result = Arguments.getSearchMethod(is, System.out, retryPolicy);
        assertEquals("the", result._1);
        assertTrue(result._2.isPresent());
        assertEquals(result._2.get(), Arguments.Method.StringMatch);
    }

    @Test
    public void getSearchMethodRetries() {
        InputStream is = new ByteArrayInputStream("\n\n".getBytes(StandardCharsets.UTF_8));
        val result = Arguments.getSearchMethod(is, System.out, retryPolicy);
        assertTrue(result._1.isEmpty());
        assertFalse(result._2.isPresent());
    }

    @Test
    public void getSearchRetriesThenPasses() {
        InputStream is = new ByteArrayInputStream("\nthe\n1".getBytes(StandardCharsets.UTF_8));
        val result = Arguments.getSearchMethod(is, System.out, retryPolicy);
        assertFalse(result._1.isEmpty());
        assertTrue(result._2.isPresent());
    }

    @Test
    public void getSearchMethodRegex() {
        InputStream is = new ByteArrayInputStream("the\n2".getBytes(StandardCharsets.UTF_8));
        val result = Arguments.getSearchMethod(is, System.out, retryPolicy);
        assertEquals(result._2.get(), Arguments.Method.RegexMatch);
    }

    @Test
    public void getSearchMethodIndexed() {
        InputStream is = new ByteArrayInputStream("the\n3".getBytes(StandardCharsets.UTF_8));
        val result = Arguments.getSearchMethod(is, System.out, retryPolicy);
        assertEquals(result._2.get(), Arguments.Method.Indexed);
    }

    @Test
    public void getSearchMethodEmpty() {
        InputStream is = new ByteArrayInputStream("the\n\n4".getBytes(StandardCharsets.UTF_8));
        val result = Arguments.getSearchMethod(is, System.out, retryPolicy);
        assertFalse(result._2.isPresent());
    }

    @Test
    public void getSearchIOException () throws IOException {
        InputStream is = new ByteArrayInputStream("the\n1".getBytes(StandardCharsets.UTF_8));
        InputStream mockInputStream = spy(new ByteArrayInputStream("the\n3".getBytes(StandardCharsets.UTF_8)));
        RetryPolicy mockRetry = mock(RetryPolicy.class);
        doThrow(new IOException()).when(mockInputStream).close();
        val result = Arguments.getSearchMethod(mockInputStream, System.out, retryPolicy);
        assertFalse(result._2.isPresent());
    }
}