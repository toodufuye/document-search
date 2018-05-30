package search;

import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.CoreMap;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

@Value.Immutable
public abstract class Tokenizer {
    private static final Logger logger = LogManager.getLogger(Tokenizer.class);
    public abstract File file();

    // this value is cached automatically by lombok

//    private final Either<Exception, List<CoreLabel>> cached = getTokens();

    @Value.Lazy
    public Either<Exception, List<CoreLabel>> tokens() {
        try (Reader reader = Files.asCharSource(this.file(), Charset.defaultCharset()).openStream()) {
            return Right(List.ofAll(new PTBTokenizer<>(reader, new CoreLabelTokenFactory(), "").tokenize()));
        } catch (IOException io) {
            // This branch is very difficult to hit in tests, even via mocks.  The reason is that the InputStream
            // is created within getTokens.  Moving the input stream creation outside of this class would help
            // with dependency injection during testing, but isn't necessarily a better solution.
            logger.error("Error occurred while reading file: ", io);
            return Left(io);
        }
    }

    String getAbsoluteFilePath() {
        return file().getAbsolutePath();
    }

    List<CoreMap> regexMatch(String input) {
        TokenSequencePattern pattern = TokenSequencePattern.compile(input);
        TokenSequenceMatcher matcher = pattern.getMatcher(tokens().right().get().toJavaList());
        List<CoreMap> result = List.empty();
        while (matcher.find()) {
            List<CoreMap> matchedTokens = List.ofAll(matcher.groupNodes());
            result = result.appendAll(matchedTokens);
        }
        return result;
    }

    List<CoreLabel> stringMatch(String input) {
        return tokens().map(x -> x.filter(y -> y.originalText().equals(input))).right().get();
    }
}
