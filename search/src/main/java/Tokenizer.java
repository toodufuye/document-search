import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.util.CoreMap;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Optional;

import static io.vavr.API.Left;
import static io.vavr.API.Right;


public class Tokenizer {
    private static final Logger logger = LogManager.getLogger(Tokenizer.class);

    private File file;
    @Getter(lazy=true) private final Either<Exception, List<CoreLabel>> cached = getTokens();

    Tokenizer(File file) {
        this.file = file;
    }

    private Either<Exception, List<CoreLabel>> getTokens() {
        try (Reader reader = Files.asCharSource(this.file, Charset.defaultCharset()).openStream()) {
            return Right(List.ofAll(new PTBTokenizer<>(reader, new CoreLabelTokenFactory(), "").tokenize()));
        } catch (IOException io){
            logger.error("Error occurred while reading file: ", io);
            return Left(io);
        }
    }

    private List<CoreMap> regexMatch(Either<Exception, List<CoreLabel>> tokens, String input) {
        TokenSequencePattern pattern = TokenSequencePattern.compile(input);
        TokenSequenceMatcher matcher = pattern.getMatcher(tokens.right().get().toJavaList());
        List<CoreMap> result = List.empty();
        while (matcher.find()) {
            List<CoreMap> matchedTokens = List.ofAll(matcher.groupNodes());
            result = result.appendAll(matchedTokens);
        }
        return result;
    }

    // Todo: Handle the case of a Left return from getTokens
    /**
     *
     * @param input search string
     * @param method search method
     * @return an Optional search result.  The default case requires a value even if it will never be hit
     * This forces me to set the result and an empty optional fits here.
     */
    Optional<SearchResult> searchTokens(String input, Arguments.Method method) {
        Optional<SearchResult> result;
        switch (method) {
            case StringMatch:
                result = Optional.of(new SearchResult(file.getAbsolutePath(),
                        getCached().map(x -> x.filter(y -> y.originalText().equals(input))).right().get().size()));
                break;
            case RegexMatch:
                result = Optional.of(new SearchResult(file.getAbsolutePath(), regexMatch(getCached(), input).size()));
                break;
            case Indexed: // Todo: make this an actual database call
                result = Optional.of(new SearchResult(file.getAbsolutePath(),
                        getCached().map(x -> x.filter(y -> y.originalText().equals(input))).right().get().size()));
                break;
            default:
                // The default should never be reached.  Just in case, it's set to the same output as StringMatch
                result = Optional.empty();
        }
        return result;
    }
}
