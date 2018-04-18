# document-search
Search for words (tokens) in text files

## Intellij setup
- Please install the intellij Lombok plugin for first class lombok support [https://plugins.jetbrains.com/plugin/6317-lombok-plugin]

## How to Build
- While in the search directory, run the command ``` ./mvnw clean install ``` 

## How to Run
- Run the command ``` java -jar <full path to jar> <full path to unzipped folder with txt files> ```
- You'll be prompted to enter a search term.  The search term can be one of the following
  - A string with no quotes.  For example: ```the```
  - A simple regex.  For example: ```/the|and/``` will search for both the tokens ```the``` and ```and```
  - A compound token pattern.  For example ```[ !{ tag:/VB.*/ } ]``` will search for all tokens that are not verbs.  [ Documentation](https://nlp.stanford.edu/software/tokensregex.html)
- You'll be prompted to choose a search method.  Your choices are
  - String (basic string matching)
  - Regex (Necessary if your search term is a regex)
  - Indexed (The tokens are first indexed in an H2 database before being queried)

## Code Coverage Report
- First run ```./mvnw clean test``` then open the file search/target/site/jacoco/index.html in your browser of choice

## Assumptions
- Text files that you want to be analyzed end in a ```.txt``` extension

## Performance Testing
- To run performance tests, execute the following ```./mvnw -Dtest=TokenizerTest#performanceTest -DargLine="-Dsystem.performance.test=true" test```

## Scaling considerations
- Change from using H2 as a database to either a relational database, or a NOSQL database like DynamoDb.
- Separate the Loading of data into the database into its own separate and scalable process.
- Consider removing the ability to read from the filesystem directly.  If the filesystem is necessary, consider a distributed batch processing cluster like Apache Spark / HDFS.
- Add an api and an api (ex: nginx) in front of the application.  Allow input to be given in a better way than the console.
- The reading of files and processing of tokens is a highly parallelizable operation.  Take advantage of parallelism & concurrency to process large amounts of files.

## Library credits
- [Angel X. Chang and Christopher D. Manning. 2014. TokensRegex: Defining cascaded regular expressions over tokens. Stanford University Technical Report, 2014.](https://nlp.stanford.edu/pubs/tokensregex-tr-2014.bib)
- [Project Lombok](https://projectlombok.org/)
- [Vavr](http://www.vavr.io/)
- [Failsafe](https://github.com/jhalterman/failsafe)
- [Google Guava](https://github.com/google/guava)
- [JOpt Simple](https://pholser.github.io/jopt-simple/examples.html)
- [Jdbi 3](http://jdbi.org/)
- [H2 Database](http://www.h2database.com/html/main.html)

