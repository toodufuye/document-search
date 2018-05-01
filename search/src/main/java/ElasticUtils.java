import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.ling.CoreLabel;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Try;
import models.DocumentToken;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

class ElasticUtils {
    private static final Logger logger = LogManager.getLogger(ElasticUtils.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    static boolean indexCreatedAndUpdated = false;

    static boolean indexExists(String elasticURL) {
        return !Try.of(() -> Request.Get(elasticURL).execute().returnContent().asString().contains("index_not_found_exception")).getOrElse(true);
    }

    static void createIndex(String elasticURL) {
        String indexSettings = "{\"mappings\": {\"_doc\": {\"properties\": {\"fileName\": {\"type\": \"keyword\"},\"token\": {\"type\": \"text\", \"analyzer\": \"whitespace\"}}}}}";
        try {
            Response response = Request.Put(elasticURL)
                    .bodyString(indexSettings, ContentType.APPLICATION_JSON)
                    .execute();
            logger.info(response.returnContent());
        } catch (Exception e) {
            logger.error("Error occurred while creating index", e);
        }
    }

    static void insertIntoElasticSearch(List<File> files, RetryPolicy retryPolicy, String elasticURL) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        logger.info("Storing tokens into elasticSearch");
        files.forEach(file -> {
            Either<Exception, List<CoreLabel>> tokens = Tokenizer.builder()
                    .file(file)
                    .build()
                    .getCached();

            tokens.right()
                    .forEach((List<CoreLabel> y) -> y.forEach((CoreLabel z) -> Failsafe
                                    .with(retryPolicy
                                            .retryOn(ClientProtocolException.class)
                                            .retryOn(IOException.class))
                                    .onFailedAttempt(failure -> logger.error(
                                            "Error occurred while interacting with Elastic search", failure))
                                    .get(() -> Request.Put(
                                            String.format("%s/_doc/%s", elasticURL, atomicInteger.incrementAndGet()))
                                            .bodyString(
                                                    objectMapper.writeValueAsString(
                                                            new DocumentToken(file.getAbsolutePath(), z.originalText())),
                                                    ContentType.APPLICATION_JSON)
                                            .execute()).discardContent()
                    ));

            // I can't hit tokens.left() via testing because I have not gotten an exception to occur while
            // reading files.  Reasoning is explained in the Tokenizer.getTokens() method.
            tokens.left()
                    .forEach(x -> logger.error(
                            "Error occurred while retrieving tokens.  Cannot add tokens to the database", x));
        });
    }
}
