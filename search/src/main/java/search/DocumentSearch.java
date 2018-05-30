package search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.vavr.collection.List;
import io.vavr.control.Try;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;
import search.models.Arguments;
import search.models.*;
import search.models.elasticResponse.AggResponse;
import search.models.elasticResponse.Bucket;

import java.io.*;
import java.util.Iterator;

@Value.Immutable
public abstract class DocumentSearch {
    private static final Logger logger = LogManager.getLogger(DocumentSearch.class);
    private final ObjectMapper mapper = new ObjectMapper();
    public abstract RetryPolicy retryPolicy();
    public abstract String directory();
    public abstract InputStream in();
    public abstract PrintStream out();
    public abstract String elasticURL();

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
        return ImmutableArguments.builder().input(input).method(method).build();
    }

    List<SearchResult> searchTokens(List<File> files, Arguments arguments) {
        String elasticSearchString = String.format(
                "{\n\"query\":{\n\"term\":{\"content\":\"%s\"}\n},\n\"aggs\":{" +
                        "\n\"files\":{\n\"terms\":{\"field\":\"fileName\"}\n}\n}\n}", arguments.input());
        List<SearchResult> result;
        switch (arguments.method()) {
            case StringMatch:
                result = files.map(x -> ImmutableTokenizer.builder().file(x).build())
                        .map(x -> ImmutableSearchResult.builder()
                                .fileName(x.getAbsoluteFilePath())
                                .occurrences(x.stringMatch(arguments.input()).size()).build());
                break;
            case RegexMatch:
                result = files.map(x -> ImmutableTokenizer.builder().file(x).build())
                        .map(x -> ImmutableSearchResult.builder()
                                .fileName(x.getAbsoluteFilePath())
                                .occurrences(x.regexMatch(arguments.input()).size()).build());
                break;
            case Indexed:
                String elasticResponse = Try.of(() -> Request.Post(String.format("%s/_search?size=0", elasticURL()))
                        .bodyString(elasticSearchString, ContentType.APPLICATION_JSON)
                        .execute().returnContent().toString()).getOrElse("{}");
                result = Try.of(() -> List.ofAll(mapper.readValue(elasticResponse, AggResponse.class)
                        .aggregations()
                        .files()
                        .buckets())
                        .<SearchResult>map((Bucket x) -> ImmutableSearchResult.builder().fileName(x.key()).occurrences(x.doc_count()).build()))
                        .getOrElse(List::empty);
                break;
            default:
                result = List.empty();
        }
        return result;
    }

    String search() {
        Iterator<File> fileIterator = Files.fileTraverser()
                .breadthFirst(new File(this.directory()))
                .iterator();

        // There is a potential to exhaust all open file handles on a system here
        // If there is any chance of that happening, it is better to use the file iterator instead of turning it
        // into a list.  For the purposes of this case study, this should be sufficient.
        List<File> files = List.ofAll(Lists.newArrayList(fileIterator))
                .filter(File::isFile)
                .filter(x -> x.getName().endsWith(".txt"));

        if (!ElasticUtils.indexExists(elasticURL())) {
            ElasticUtils.createIndex(elasticURL());
            ElasticUtils.insertIntoElasticSearch(files, retryPolicy(), elasticURL());
        }

        if (!files.isEmpty()) {
            Arguments arguments = getSearchMethod(in(), out(), retryPolicy());
            if (arguments.input().isEmpty()) {
                return "Please provide a non empty string for the search term";
            } else {
                Long startTime = System.currentTimeMillis();
                return searchTokens(files, arguments)
                        .sortBy(SearchResult::occurrences)
                        .reverse()
                        .map(x -> String.format("%s - %s matches", x.fileName(), x.occurrences()))
                        .mkString("\n")
                        .concat(String.format("\nElasped time: %s ms", System.currentTimeMillis() - startTime));
            }
        } else {
            return String.format("There are no text files in the directory %s or the directory does not exist", this.directory());
        }
    }
}
