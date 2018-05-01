import com.google.common.collect.Lists;
import com.google.common.io.Files;
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

@Builder
class DocumentSearch {
    private static final Logger logger = LogManager.getLogger(DocumentSearch.class);
    private RetryPolicy retryPolicy;
    private String directory;
    private String elasticURL;
    private InputStream in;
    private PrintStream out;

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
                        out.print("search Method: 1) String Match, 2) Regular Expression, 3) Indexed ");
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

    List<SearchResult> searchTokens(List<File> files, Arguments arguments) {
        List<SearchResult> result;
        switch (arguments.getMethod()) {
            case StringMatch:
                result = files.map(x -> Tokenizer.builder().file(x).build())
                        .map(x -> new SearchResult(x.getAbsoluteFilePath(), x.stringMatch(arguments.getInput()).size()));
                break;
            case RegexMatch:
                result = files.map(x -> Tokenizer.builder().file(x).build())
                        .map(x -> new SearchResult(x.getAbsoluteFilePath(), x.regexMatch(arguments.getInput()).size()));
                break;
            case Indexed:
                result = List.empty();
                break;
            default:
                result = List.empty();

        }
        return result;
    }

    String search() {
        Iterator<File> fileIterator = Files.fileTraverser()
                .breadthFirst(new File(directory))
                .iterator();

        // There is a potential to exhaust all open file handles on a system here
        // If there is any chance of that happening, it is better to use the file iterator instead of turning it
        // into a list.  For the purposes of this case study, this should be sufficient.
        List<File> files = List.ofAll(Lists.newArrayList(fileIterator))
                .filter(File::isFile)
                .filter(x -> x.getName().endsWith(".txt"));

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
                return searchTokens(files, arguments)
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
