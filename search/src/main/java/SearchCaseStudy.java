import com.google.common.collect.Lists;
import com.google.common.io.Files;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SearchCaseStudy {
    private static final Logger logger = LogManager.getLogger("SearchCaseStudy");

    public static void main(String[] args) {
        final OptionParser optionParser = new OptionParser();
        optionParser.acceptsAll(Arrays.asList("d", "directory"), "path to directory of text files")
                .withRequiredArg()
                .required();

        try {
            final OptionSet optionSet = optionParser.parse(args);
            final String directory = optionSet.valueOf("directory").toString();
            Iterator<File> fileIterator = Files.fileTraverser()
                    .breadthFirst(new File(directory))
                    .iterator();

            // Todo: Explain how consuming the iterator can affect performance with large quantities of files, and how it can reach the limit on file handles
            List<File> files = Lists.newArrayList(fileIterator)
                    .stream()
                    .filter(File::isFile)
                    .filter(x -> x.getName().endsWith(".txt"))
                    .collect(Collectors.toList());

            if (!files.isEmpty()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                    System.out.print("Enter the search term: ");
                    String input = br.readLine();


                    RetryPolicy retryPolicy = new RetryPolicy()
                            .retryOn(NumberFormatException.class)
                            .withDelay(1, TimeUnit.SECONDS)
                            .withMaxRetries(3);

                    int method = Failsafe.with(retryPolicy)
                            .onFailedAttempt(failure -> System.out.println(String.format("Error %s. Please try again",
                                    failure)))
                            .get(() -> {
                                System.out.print("Search Method: 1) String Match, 2) Regular Expression, 3) Indexed ");
                                return Integer.parseInt(br.readLine());
                            });

                    System.out.println(String.format("Search term: %s, Method: %s", input, method));

                } catch (IOException io) {
                    logger.error("Error occurred while reading input from the console", io);
                } catch (NumberFormatException ne) {
                    logger.error("\nEnding document search. Please try again with a valid Search Method");
                }
            } else {
                logger.info(String.format("There are no text files in the directory %s", directory));
            }


        } catch (OptionException oe) {
            try {
                optionParser.printHelpOn(System.out);
            } catch (IOException io) {
                logger.error("Exception occured while attempting to print to system out", io);
            }
        }


    }
}
