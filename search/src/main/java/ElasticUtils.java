import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreLabel;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Try;
import models.Document;
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
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

class ElasticUtils {
    private static final Logger logger = LogManager.getLogger(ElasticUtils.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    static boolean indexExists(String elasticURL) {
        return !Try.of(() -> Request.Get(elasticURL).execute().returnContent().asString().contains("index_not_found_exception")).getOrElse(true);
    }

    static void createIndex(String elasticURL) {
        String indexSettings = "{\"mappings\": {\"_doc\": {\"properties\": {\"fileName\": {\"type\": \"keyword\"},\"content\": {\"type\": \"text\", \"analyzer\": \"standard\"}}}}}";
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
        files.forEach(file -> Failsafe
                .with(retryPolicy
                        .retryOn(ClientProtocolException.class)
                        .retryOn(IOException.class))
                .onFailedAttempt(failure -> logger.error("Error occurred while storing docs in elastic search"))
                .get(() -> Request.Put(String.format("%s/_doc/%s", elasticURL, atomicInteger.incrementAndGet()))
                        .bodyString(
                                objectMapper.writeValueAsString(
                                        new Document(
                                                file.getAbsolutePath(),
                                                Files.asCharSource(file, Charset.defaultCharset()).read())),
                                ContentType.APPLICATION_JSON)
                        .execute().returnContent()));
    }
}
