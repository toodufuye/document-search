import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Builder;
import models.Arguments;
import models.Method;
import models.SearchResult;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;

import java.io.*;
import java.util.Iterator;
import java.util.Optional;

@Builder
class DocumentSearch {
    private static final Logger logger = LogManager.getLogger(DocumentSearch.class);
    private RetryPolicy retryPolicy;
    private String directory;
    private String elasticURL;
    private InputStream in;
    private PrintStream out;
    private Jdbi jdbi;

    static Arguments getSearchMethod(InputStream in, PrintStream out, RetryPolicy retryPolicy) {
        String input = "";
        Method method;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            input = Failsafe.with(retryPolicy.retryWhen(""))
                    .onFailedAttempt(failure -> out.println("Please enter a non empty string"))
                    .get(() -> {
                        out.print("Enter the search term: ");
                        return br.readLine();
                    });

            method = Failsafe.with(retryPolicy.retryWhen(null).retryOn(NumberFormatException.class))
                    .onFailedAttempt(failure -> out.println(String.format("Error %s. Please try again",
                            failure)))
                    .get(() -> {
                        out.print("Search Method: 1) String Match, 2) Regular Expression, 3) Indexed ");
                        return Method.getEndpoint(Integer.parseInt(br.readLine()));
                    });
        } catch (IOException io) {
            logger.error("Error occurred while reading input from the console.  Setting Method to String Match", io);
            method = Method.StringMatch;
        } catch (NumberFormatException ne) {
            logger.error("Error occurred while formatting number.  Setting Method to String Match", ne);
            method = Method.StringMatch;
        }
        return Arguments.builder().input(input).method(method).build();
    }

    String Search() {
        Iterator<File> fileIterator = Files.fileTraverser()
                .breadthFirst(new File(directory))
                .iterator();

        // There is a potential to exhaust all open file handles on a system here
        // If there is any chance of that happening, it is better to use the file iterator instead of turning it
        // into a list.  For the purposes of this case study, this should be sufficient.
        List<File> files = List.ofAll(Lists.newArrayList(fileIterator))
                .filter(File::isFile)
                .filter(x -> x.getName().endsWith(".txt"));

        if (!H2Utils.isInserted) {
            H2Utils.insertWordsIntoDatabase(jdbi, files);
            H2Utils.isInserted = true;
        }

//        if (!ElasticUtils.indexCreatedAndUpdated) {
//            ElasticUtils.createIndex(elasticURL);
//            ElasticUtils.insertIntoElasticSearch(files, retryPolicy, elasticURL);
//        }

        if (!files.isEmpty()) {
            Arguments arguments = getSearchMethod(in, out, retryPolicy);
            if (arguments.getInput().isEmpty()) {
                return "Please provide a non empty string for the search term";
            } else {
                Long startTime = System.currentTimeMillis();
                // this can be parallel mapped for speed improvements given a large enough list of files and the processing cores to support it
                return files.map(x -> Tokenizer.builder().file(x).jdbi(jdbi).build())
                        .map(x -> x.searchTokens(arguments.getInput(), arguments.getMethod())) // the second tuple argument will be present in this else case.
                        .map(Optional::get)
                        .sortBy(SearchResult::getOccurrences)
                        .reverse()
                        .map(x -> String.format("%s - %s matches", x.getFileName(), x.getOccurrences()))
                        .mkString("\n")
                        .concat(String.format("\nElasped time: %s ms", System.currentTimeMillis() - startTime));
            }
        } else {
            return String.format("There are no text files in the directory %s or the directory does not exist", directory);
        }
    }
}
