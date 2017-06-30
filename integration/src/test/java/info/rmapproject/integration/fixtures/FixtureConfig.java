package info.rmapproject.integration.fixtures;

import info.rmapproject.spring.triplestore.support.SesameTriplestoreManager;
import info.rmapproject.spring.triplestore.support.TriplestoreManager;
import okhttp3.OkHttpClient;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides a Spring context configuration.  Provides test fixture beans, and other beans defined in RMap modules.
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Configuration
public class FixtureConfig {

    @Autowired
    private Environment env;

    /**
     * Provides a {@link TriplestoreManager} that can be used by integration tests to create, remove, or clear the
     * contents of the triplestore shared by the RMap API and HTML UI web applications.
     *
     * @return a {@code TriplestoreManager} instance
     */
    @Bean
    public TriplestoreManager triplestoreManager() {
        SesameTriplestoreManager manager = new SesameTriplestoreManager();
        manager.setDefaultName(env.getProperty("sesamehttp.repository.name"));
        manager.setHttpClient(httpClient());
        try {
            manager.setRepositoryBaseUrl(new URL(env.getProperty("sesamehttp.repository.url")));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            manager.setWorkbenchBaseUrl(new URL(env.getProperty("sesamehttp.workbench.url")));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return manager;
    }

    /**
     * Provides a {@link OkHttpClient} that can be used by integration tests to connect to the RMap web applications
     * under test, or resolve any HTTP-accessible resources necessary for supporting the integration testing of RMap.
     *
     * @return the {@code OkHttpClient}
     */
    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient();
    }

    /**
     * Provides a {@link DataSource} to the database shared by the RMap API and HTML UI web applications.
     *
     * @return the {@code DataSource}
     */
    @Bean
    public DataSource ds() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(env.getProperty("authdb.driverClassName"));
        ds.setUrl(env.getProperty("authdb.url"));
        ds.setUsername(env.getProperty("authdb.username"));
        ds.setPassword(env.getProperty("authdb.password"));
        ds.setValidationQuery(env.getProperty("authdb.validationQuery"));
        ds.setTestOnBorrow(Boolean.valueOf(env.getProperty("authdb.testOnBorrow")));
        return ds;
    }

}
