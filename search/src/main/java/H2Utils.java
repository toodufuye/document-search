import db.WordDao;
import edu.stanford.nlp.ling.CoreLabel;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;
import java.io.File;

public class H2Utils {
    private static final Logger logger = LogManager.getLogger(H2Utils.class);
    public static void insertWordsIntoDatabase(Jdbi jdbi, List<File> files) {
        jdbi.useExtension(WordDao.class, dao -> {
            dao.createTable();
            dao.createIndex();
            files.forEach(file -> {
                Either<Exception, List<CoreLabel>> tokens = Tokenizer.builder()
                        .file(file)
                        .jdbi(jdbi)
                        .build()
                        .getCached();
                // Either projections only get executed if the projection matches the actual value.
                tokens.right()
                        .forEach((List<CoreLabel> y) -> y.forEach((CoreLabel z) -> dao.insert(
                                z.originalText(),
                                file.getAbsolutePath())));
                tokens.left()
                        .forEach(x -> logger.error(
                                "Error occurred while retrieving tokens.  Cannot add tokens to the database", x));
            });
        });
    }
}
