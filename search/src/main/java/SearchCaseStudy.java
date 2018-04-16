import com.google.common.collect.Lists;
import com.google.common.io.Files;
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
            Iterator<File> fileIterator = Files.fileTraverser()
                    .breadthFirst(new File(optionSet.valueOf("directory").toString()))
                    .iterator();

            // Todo: Explain how consuming the iterator can affect performance with large quantities of files, and how it can reach the limit on file handles
            List<File> files = Lists.newArrayList(fileIterator)
                    .stream()
                    .filter(File::isFile)
                    .filter(x -> x.getName().endsWith(".txt"))
                    .collect(Collectors.toList());
            System.out.println(String.format("The directory is: %s and it contains files: %s",
                    optionSet.valueOf("directory"),
                    files));

        } catch (OptionException oe) {
            try {
                optionParser.printHelpOn(System.out);
            } catch (IOException io) {
                logger.info("Exception occured while attempting to print to system out", io);
            }
        }


    }
}
