import io.vavr.Tuple;
import io.vavr.Tuple2;
import models.Method;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Optional;

class Arguments {
    private static final Logger logger = LogManager.getLogger(Arguments.class);

    static Tuple2<String, Optional<Method>> getSearchMethod(InputStream in, PrintStream out, RetryPolicy retryPolicy) {
        String input = "";
        Optional<Method> method;
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
                        out.print("Search Method: 1) String Match, 2) Regular Expression, 3) Indexed ");
                        return models.Method.getEndpoint(Integer.parseInt(br.readLine()));
                    });
        } catch (IOException io) {
            logger.error("Error occurred while reading input from the console", io);
            method = Optional.empty();
        } catch (NumberFormatException ne) {
            logger.error("Error occurred while formatting number", ne);
            method = Optional.empty();
        }
        return Tuple.of(input, method);
    }
}
