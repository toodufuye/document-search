import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Builder;
import net.jodah.failsafe.RetryPolicy;


import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Optional;

@Builder
class DocumentSearch {
    private RetryPolicy retryPolicy;
    private String directory;
    private InputStream in;
    private PrintStream out;

    String Search() {
        Iterator<File> fileIterator = Files.fileTraverser()
                .breadthFirst(new File(directory))
                .iterator();

        // Todo: Explain how consuming the iterator can affect performance with large quantities of files, and how it can reach the limit on file handles
        List<File> files = List.ofAll(Lists.newArrayList(fileIterator))
                .filter(File::isFile)
                .filter(x -> x.getName().endsWith(".txt"));

        if (!files.isEmpty()) {
            Tuple2<String, Optional<Arguments.Method>> arguments = Arguments.getSearchMethod(
                    in,
                    out,
                    retryPolicy);
            if (arguments._1.isEmpty()) {
                return "Please provide a non empty string for the search term";
            } else if (!arguments._2.isPresent()) {
                return "Please provide a valid search method";
            } else {
                // Success case
                    return files.map(Tokenizer::new)
                        .map(x -> x.searchTokens(arguments._1, arguments._2.get()))
                        .sortBy(SearchResult::getOccurrences)
                        .reverse()
                        .map(x -> String.format("%s - %s matches", x.getFileName(), x.getOccurrences())).mkString("\n");
            }
        } else {
            return String.format("There are no text files in the directory %s", directory);
        }
    }
}
