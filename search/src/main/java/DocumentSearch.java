import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Builder;
import models.Method;
import models.SearchResult;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
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

//    Optional<SearchResult> searchTokens(String input, File file, Arguments.Method method) {
//        Optional<SearchResult> result;
//        switch (method) {
//            case StringMatch:
//                result = Optional.of(new SearchResult(file.getAbsolutePath(),
//                        getCached().map(x -> x.filter(y -> y.originalText().equals(input))).right().get().size()));
//                break;
//            case RegexMatch:
//                result = Optional.of(new SearchResult(file.getAbsolutePath(), regexMatch(getCached(), input).size()));
//                break;
//            case Indexed:
//                result = Optional.of(new SearchResult(
//                        file.getAbsolutePath(),
//                        jdbi.withExtension(
//                                WordDao.class,
//                                dao -> dao.countOccurences(input, file.getAbsolutePath()))));
//                break;
//            default:
//                // The default should never be reached.  Just in case, it's set to an empty Optional
//                result = Optional.empty();
//        }
//        return result;
//    }

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

        if (!ElasticUtils.indexCreatedAndUpdated) {
            ElasticUtils.createIndex(elasticURL);
            ElasticUtils.insertIntoElasticSearch(files, retryPolicy, elasticURL);
        }

        if (!files.isEmpty()) {
            Tuple2<String, Optional<Method>> arguments = Arguments.getSearchMethod(
                    in,
                    out,
                    retryPolicy);
            if (arguments._1.isEmpty()) {
                return "Please provide a non empty string for the search term";
            } else if (!arguments._2.isPresent()) {
                return "Please provide a valid search method";
            } else {
                Long startTime = System.currentTimeMillis();
                // this can be parallel mapped for speed improvements given a large enough list of files and the processing cores to support it
                return files.map(x -> Tokenizer.builder().file(x).jdbi(jdbi).build())
                        .map(x -> x.searchTokens(arguments._1, arguments._2.get())) // the second tuple argument will be present in this else case.
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
