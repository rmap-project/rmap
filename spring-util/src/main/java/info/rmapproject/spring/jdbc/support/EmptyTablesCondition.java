package info.rmapproject.spring.jdbc.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Checks the supplied {@link DataSource} for any tables, and returns {@code true} if the tables are empty.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class EmptyTablesCondition implements Function<DataSource, Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(EmptyTablesCondition.class);

    @Override
    public Boolean apply(DataSource dataSource) {

        try (Connection c = dataSource.getConnection()) {

            final List<String> tables = new ArrayList<>();
            final DatabaseMetaData meta = c.getMetaData();
            StringBuilder message = new StringBuilder();

            try (ResultSet tableRs = meta.getTables(
                    null, null, null, new String[]{"TABLE"})) {

                message.append("Database contains the following tables: ");
                while (tableRs.next()) {
                    String tableName = tableRs.getString("TABLE_NAME");
                    tables.add(tableName);
                    message.append(String.format("'%s' ", tableName));
                }

            }

            LOG.debug(message.toString());
            message.delete(0, message.length());

            final AtomicBoolean emptyTables = new AtomicBoolean(Boolean.TRUE);

            tables.forEach(tableName -> {

                try {

                    message.append(String.format("Checking table '%s' for data ... ", tableName));
                    PreparedStatement ps = c.prepareStatement(String.format("SELECT * FROM %s", tableName));
                    boolean hasData = false;
                    try (ResultSet rs = ps.executeQuery()) {
                        hasData = rs.next();
                    }

                    if (hasData) {
                        emptyTables.set(Boolean.FALSE);
                    }

                    message.append(String.format("'%s'\n", hasData));
                    LOG.debug(message.toString());
                    message.delete(0, message.length());

                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

            });

            return emptyTables.get();

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

}
