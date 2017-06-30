package info.rmapproject.auth.dao;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring-rmapauth-context.xml")
public class ApiKeyDaoImplTest {

    @Autowired
    private ApiKeyDaoImpl underTest;

    @Test
    public void testFoo() throws Exception {
        assertNotNull(underTest);
    }
}