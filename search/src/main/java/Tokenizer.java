import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.CoreMap;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

@Builder
class Tokenizer {
    private static final Logger logger = LogManager.getLogger(Tokenizer.class);

    private File file;

    // this value is cached automatically by lombok
    @Getter(lazy = true)
    private final Either<Exception, List<CoreLabel>> cached = getTokens();

    private Either<Exception, List<CoreLabel>> getTokens() {
        try (Reader reader = Files.asCharSource(this.file, Charset.defaultCharset()).openStream()) {
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
        return file.getAbsolutePath();
    }

    List<CoreMap> regexMatch(String input) {
        TokenSequencePattern pattern = TokenSequencePattern.compile(input);
        TokenSequenceMatcher matcher = pattern.getMatcher(getTokens().right().get().toJavaList());
        List<CoreMap> result = List.empty();
        while (matcher.find()) {
            List<CoreMap> matchedTokens = List.ofAll(matcher.groupNodes());
            result = result.appendAll(matchedTokens);
        }
        return result;
    }

    List<CoreLabel> stringMatch(String input) {
        return getCached().map(x -> x.filter(y -> y.originalText().equals(input))).right().get();
    }
}
