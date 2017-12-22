package info.rmapproject.integration.util;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.InetAddress;

/**
 * Simple Ant task which starts a Derby network server.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class StartDerbyTask extends Task {

    private static final Logger LOG = LoggerFactory.getLogger(StartDerbyTask.class);

    private static final String SPRING_ACTIVE_PROFILES = "spring.profiles.active";

    private static final String SESSION_FACTORY_BEAN_NAME = "integrationSessionFactory";

    private int port = 1527;

    private String host = "localhost";

    private File derbyHome;

    private String springProfiles;

    private String springContextLocation;

    public void execute() throws BuildException {
        if (derbyHome == null) {
            throw new IllegalStateException("'derbyHome' is a required attribute!");
        }

        NetworkServerControl server = null;
        try {
            System.setProperty("derby.system.home", derbyHome.getAbsolutePath());
            server = new NetworkServerControl(InetAddress.getByName(host), port);
            server.start(null);
            LOG.info("Started Derby Network Server (derby.system.home={}) on {}:{}",
                    derbyHome.getAbsolutePath(), host, port);
        } catch (Exception e) {
            throw new BuildException(
                    String.format(
                            "Error starting the Derby Network Server (derby.system.home=%s) on %s:%s: %s",
                            derbyHome.getAbsolutePath(), host, port, e.getMessage()), e);
        }

        if (springContextLocation == null) {
            return;
        }

        if (springProfiles != null && springProfiles.trim().length() > 0) {
            System.setProperty(SPRING_ACTIVE_PROFILES, springProfiles);
        }

        ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(springContextLocation);
        LOG.info("Acquired SessionFactory: " + appCtx.getBean(SESSION_FACTORY_BEAN_NAME).getClass().getName());

    }

    /**
     * The TCP port Derby shall be started on.
     *
     * @return the TCP port, defaults to {@code 1527}
     */
    public int getPort() {
        return port;
    }

    /**
     * The TCP port Derby shall be started on.
     *
     * @param port the TCP port
     */
    public void setPort(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("'port' must be a positive integer!");
        }
        this.port = port;
    }

    /**
     * The TCP host Derby shall be started on.
     *
     * @return the TCP host, defaults to {@code localhost}
     */
    public String getHost() {
        return host;
    }

    /**
     * The TCP host Derby shall be started on.
     *
     * @param host the TCP host
     */
    public void setHost(String host) {
        if (host == null || host.trim().length() == 0) {
            throw new IllegalArgumentException("'host' must not be empty or null!");
        }
        this.host = host;
    }

    /**
     * The directory used by Derby to persist state and log information.
     *
     * @return the Derby home directory
     */
    public File getDerbyHome() {
        return derbyHome;
    }

    /**
     * The directory used by Derby to persist state and log information.
     *
     * @param derbyHome the Derby home directory
     */
    public void setDerbyHome(File derbyHome) {
        if (derbyHome == null) {
            throw new IllegalArgumentException("'derbyHome' must not be null!");
        }
        this.derbyHome = derbyHome;
    }

    public String getSpringProfiles() {
        return springProfiles;
    }

    public void setSpringProfiles(String springProfiles) {
        this.springProfiles = springProfiles;
    }

    public String getSpringContextLocation() {
        return springContextLocation;
    }

    public void setSpringContextLocation(String springContextLocation) {
        this.springContextLocation = springContextLocation;
    }
}
