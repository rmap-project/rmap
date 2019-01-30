package info.rmapproject.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Simple tests insuring that the Spring wiring for the embedded database is working properly, and that the database
 * is initialized with some data per {@code <jdbc:initialize-database> ... </jdbc:initialize-database>} in the
 * Spring context.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DataSourceConnectionIT extends AuthDBAbstractIT {

    @Autowired
    private DataSource underTest;

    @Test
    public void testSimpleConnection() throws Exception {
        final Connection c = underTest.getConnection();
        assertNotNull(c);
        c.close();
    }

    @Test
    public void testMultipleConnections() throws Exception {
        Connection c1 = underTest.getConnection();
        Connection c2 = underTest.getConnection();

        assertNotNull(c1);
        assertNotNull(c2);

        c1.close();
        c2.close();
    }

    @Test
    public void testSimpleSelectQuery() throws Exception {
        Connection c = underTest.getConnection();
        ResultSet rs = c.prepareCall("SELECT 1").executeQuery();
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
    }

    @Test
    public void testTableExists() throws Exception {
        Connection c = underTest.getConnection();
        ResultSet rs = c.prepareCall("SELECT * from Users").executeQuery();
        assertTrue(rs.next());
        rs.close();
    }
}
