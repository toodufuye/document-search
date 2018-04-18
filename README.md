# document-search
Search for words (tokens) in text files

## Intellij setup
- Please install the intellij Lombok plugin for first class lombok support [https://plugins.jetbrains.com/plugin/6317-lombok-plugin]

## How to Build
- While in the search directory, run the command ``` ./mvnw clean install ``` 

## How to Run
- Run the command ``` java -jar <full path to jar> <full path to unzipped folder with txt files> ```

## Assumptions
- Text files that you want to be analyzed end in a ```.txt``` extension


## Library credits
- Angel X. Chang and Christopher D. Manning. 2014. TokensRegex: Defining cascaded regular expressions over tokens. Stanford University Technical Report, 2014. [https://nlp.stanford.edu/pubs/tokensregex-tr-2014.bib]
- Project Lombok [https://projectlombok.org/]
- Vavr [http://www.vavr.io/]
- Failsafe [https://github.com/jhalterman/failsafe]
- Google Guava [https://github.com/google/guava]
- JOpt Simple [https://pholser.github.io/jopt-simple/examples.html]
- Jdbi 3 [http://jdbi.org/]
- H2 Database [http://www.h2database.com/html/main.html]

