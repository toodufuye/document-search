package search.db;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface WordDao {
    @SqlUpdate("CREATE TABLE if not exists word (id BIGINT auto_increment PRIMARY KEY, value VARCHAR, file VARCHAR)")
    void createTable();

    @SqlUpdate("CREATE INDEX if not exists value_idx ON word(value)")
    void createIndex();

    @SqlUpdate("INSERT INTO word (value, file) VALUES (:value, :file)")
    void insert(@Bind("value") String value, @Bind("file") String file);

    @SqlQuery("SELECT count(*) from word where value = ? and file = ?")
    Integer countOccurences(String value, String file);
}
