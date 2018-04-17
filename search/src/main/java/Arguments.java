import io.vavr.Tuple;
import io.vavr.Tuple2;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class Arguments {
    private static final Logger logger = LogManager.getLogger(Arguments.class);

    enum Method {
        StringMatch(1),
        RegexMatch(2),
        Indexed(3);

        public final int num;

        Method(int num) {
            this.num = num;
        }

        static Optional<Method> getEndpoint(int num) {
            Optional<Method> temp;
            switch (num) {
                case 1:
                    temp = Optional.of(Method.StringMatch);
                    break;
                case 2:
                    temp = Optional.of(Method.RegexMatch);
                    break;
                case 3:
                    temp = Optional.of(Method.Indexed);
                    break;
                default:
                    temp = Optional.empty();
            }
            return temp;
        }
    }

    static Tuple2<String, Optional<Method>> getSearchMethod() {
        String input;
        Optional<Method> method;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {


            RetryPolicy retryPolicy = new RetryPolicy()
                    .retryOn(NumberFormatException.class)
                    .withDelay(1, TimeUnit.SECONDS)
                    .withMaxRetries(3);

            input = Failsafe.with(retryPolicy.retryWhen(""))
                    .onFailedAttempt(failure -> System.out.println("Please enter a non empty string"))
                    .get(() -> {
                        System.out.print("Enter the search term: ");
                        return br.readLine();
                    });

            method = Failsafe.with(retryPolicy)
                    .onFailedAttempt(failure -> System.out.println(String.format("Error %s. Please try again",
                            failure)))
                    .get(() -> {
                        System.out.print("Search Method: 1) String Match, 2) Regular Expression, 3) Indexed ");
                        return Method.getEndpoint(Integer.parseInt(br.readLine()));
                    });
        } catch (IOException io) {
            logger.error("Error occurred while reading input from the console", io);
            input = "";
            method = Optional.empty();
        } catch (NumberFormatException ne) {
            input = "";
            method = Optional.empty();
        }

        return Tuple.of(input, method);
    }
}
