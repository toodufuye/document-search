package search;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.immutables.value.Value;
import search.models.SearchResult;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Optional;

@Value.Immutable
public abstract class DocumentSearch {
    private static final Logger logger = LogManager.getLogger(DocumentSearch.class);
    public abstract RetryPolicy retryPolicy();
    public abstract String directory();
    public abstract InputStream in();
    public abstract PrintStream out();
    public abstract Jdbi jdbi();

    String Search() {
        Iterator<File> fileIterator = Files.fileTraverser()
                .breadthFirst(new File(this.directory()))
                .iterator();

        // There is a potential to exhaust all open file handles on a system here
        // If there is any chance of that happening, it is better to use the file iterator instead of turning it
        // into a list.  For the purposes of this case study, this should be sufficient.
        List<File> files = List.ofAll(Lists.newArrayList(fileIterator))
                .filter(File::isFile)
                .filter(x -> x.getName().endsWith(".txt"));

        if (!H2Utils.isInserted) {
            H2Utils.insertWordsIntoDatabase(this.jdbi(), files);
            H2Utils.isInserted = true;
        }
        if (!files.isEmpty()) {
            Tuple2<String, Optional<Arguments.Method>> arguments = Arguments.getSearchMethod(
                    this.in(),
                    this.out(),
                    this.retryPolicy());
            if (arguments._1.isEmpty()) {
                return "Please provide a non empty string for the search term";
            } else if (!arguments._2.isPresent()) {
                return "Please provide a valid search method";
            } else {
                Long startTime = System.currentTimeMillis();
                // this can be parallel mapped for speed improvements given a large enough list of files and the processing cores to support it
                return files.map(x -> ImmutableTokenizer.builder().file(x).jdbi(this.jdbi()).build())
                        .map(x -> x.searchTokens(arguments._1, arguments._2.get())) // the second tuple argument will be present in this else case.
                        .map(Optional::get)
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
