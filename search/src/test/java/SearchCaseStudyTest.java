import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SearchCaseStudyTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void mainWithNoArguments() {
        SearchCaseStudy.main(new String[]{});
    }
}