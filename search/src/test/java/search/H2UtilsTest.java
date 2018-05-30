package search;

import io.vavr.collection.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.*;

public class H2UtilsTest {
    @Test
    public void createInstance() {
        new H2Utils();
        // Fun fact, static classes with no instances show up on coverage reports as a missing branch.
    }

    @Test
    public void insertWordsIntoDatabase() {
        File testFile = mock(File.class);
        Jdbi mockJdbi = mock(Jdbi.class);
        H2Utils.insertWordsIntoDatabase(mockJdbi, List.of(testFile));
        verify(mockJdbi).useExtension(any(), any());
    }
}