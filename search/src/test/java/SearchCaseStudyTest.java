import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class SearchCaseStudyTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void mainWithNoArguments() {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(bout));

        SearchCaseStudy.main(new String[]{});

        System.setOut(original);
        assertTrue(bout.toString().startsWith("Option (* = required)  Description"));
    }

    @Test
    public void mainWithArguments() {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final InputStream is = new ByteArrayInputStream("the\n1".getBytes(StandardCharsets.UTF_8));
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        System.setOut(new PrintStream(bout));
        System.setIn(is);

        SearchCaseStudy.main(new String[]{"-d", Resources.getResource("sample_files").getPath()});

        System.setOut(originalOut);
        System.setIn(originalIn);

        String output = bout.toString();
        assertTrue(output.contains("french_armed_forces.txt - 57 matches"));
        assertTrue(output.contains("hitchhikers.txt - 21 matches"));
        assertTrue(output.contains("warp_drive.txt - 6 matches"));
    }
}