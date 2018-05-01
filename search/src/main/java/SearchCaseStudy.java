import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class SearchCaseStudy {
    private static final Logger logger = LogManager.getLogger(SearchCaseStudy.class);

    public static void main(String[] args) {
        // DB_CLOSE_DELAY=-1 will keep H2 from killing the in memory database till the vm dies.
        // otherwise, the database goes away once the last connection is closed.
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        jdbi.installPlugin(new SqlObjectPlugin());

        final OptionParser optionParser = new OptionParser();
        optionParser.acceptsAll(Arrays.asList("d", "directory"), "path to directory of text files")
                .withRequiredArg()
                .required();

        RetryPolicy retryPolicy = new RetryPolicy()
                .retryOn(NumberFormatException.class)
                .withDelay(1, TimeUnit.SECONDS)
                .withMaxRetries(3);

        final String elasticURL = "http://localhost:9200/text";

        try {
            final OptionSet optionSet = optionParser.parse(args);
            final String directory = optionSet.valueOf("directory").toString();
            System.out.println(DocumentSearch.builder()
                    .directory(directory)
                    .elasticURL(elasticURL)
                    .jdbi(jdbi)
                    .in(System.in)
                    .out(System.out)
                    .retryPolicy(retryPolicy)
                    .build()
                    .Search());
        } catch (OptionException oe) {
            try {
                optionParser.printHelpOn(System.out);
            } catch (IOException io) {
                logger.error("Exception occurred while attempting to print to system out", io);
            }
        }

    }
}
