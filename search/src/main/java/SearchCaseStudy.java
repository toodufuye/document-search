import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.vavr.Tuple2;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchCaseStudy {
    private static final Logger logger = LogManager.getLogger(SearchCaseStudy.class);

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
                Tuple2<String, Optional<Arguments.Method>> arguments = Arguments.getSearchMethod();
                if (arguments._1.isEmpty()) {
                    System.out.println("Please provide a non empty string for the search term");
                } else if (!arguments._2.isPresent()) {
                    System.out.println("Please provide a valid search method");
                } else {
                    System.out.println(String.format("Search term: %s, Method: %s", arguments._1, arguments._2.get()));
                }

            } else {
                System.out.println(String.format("There are no text files in the directory %s", directory));
            }
        } catch (OptionException oe) {
            try {
                optionParser.printHelpOn(System.out);
            } catch (IOException io) {
                logger.error("Exception occurred while attempting to print to system out", io);
            }
        }
    }
}
