package info.rmapproject.spring.triplestore.support;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;

/**
 * Uses the RDF4J workbench web application to create a triplestore named according to the value of the {@link
 * #getDefaultName() default repository name} (typically this is set by the caller from the {@code rdf4jhttp.
 * repository.name} system property).  The RDF4J API is used to clear or remove the triplestore.  (The 
 * RDF4J API does not provide a mechanism for creating a triplestore.)
 * <p>
 * The creation of a triplestore works by submitting a POST against the RDF4J workbench web application, normally
 * used in an interactive fashion with a browser.
 * </p>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class Rdf4jTriplestoreManager implements TriplestoreManager {

    private static final Logger LOG = LoggerFactory.getLogger(Rdf4jTriplestoreManager.class);

    private static final String ERR_CREATE_FAILURE = "Creation of '%s' repository named '%s' failed.";

    private static final String ERR_DELETE_FAILURE = "Deletion of '%s' repository named '%s' failed.";

    private static final String ERR_REQUEST_FAILURE = "'%s' request to '%s' failed with exception: '%s'";

    private static final String ERR_REPO_EXISTS = "Repository named '{}' already exists at '{}'";

    private static final String REPOSITORY_URL_FRAGMENT = "repositories/%s";

    private static final String CREATE_URL_FRAGMENT = String.format(REPOSITORY_URL_FRAGMENT, "NONE/create");

    private String defaultType = "native";

    private String defaultName = "its";

    private String defaultIndexes = "spoc,posc,cspo";

    private URL workbenchBaseUrl;

    private URL repositoryBaseUrl;

    private OkHttpClient httpClient;

    private int readTimeoutMs = 30000;

    private int connectTimeoutMs = 30000;

    /**
     * Creates a triplestore derived from the the {@link #defaultName default name} of the repository.  If the
     * repository already exists, this method is a no-op.
     *
     * @return the URL of the repository
     */
    @Override
    public URL createTriplestore() {
        AtomicBoolean exists = new AtomicBoolean(Boolean.FALSE);
        execute(httpClient, "GET", repositoryUrl(defaultName).toString(), emptyMap(), null,
                (res) -> {
                    if (res.code() == 200) {
                        LOG.info(ERR_REPO_EXISTS, defaultName, repositoryUrl(defaultName));
                        exists.set(Boolean.TRUE);
                    }
                },
                null);

        if (exists.get()) {
            return repositoryUrl(defaultName);
        }

        return createTriplestore(defaultName);
    }

    /**
     * Clears any triples in an existing repository
     *
     * @return the URL of the cleared repository
     * @throws RuntimeException if the repository cannot be cleared, or if the repository does not exist
     */
    @Override
    public URL clearTriplestore() {
        return clearTriplestore(defaultName);
    }

    /**
     * Removes the triplestore repository and all triples in it
     *
     * @return the URL of the deleted repository
     * @throws RuntimeException if the repository cannot be deleted, or if the repository does not exist
     */
    @Override
    public URL removeTriplestore() {
        return removeTriplestore(defaultName);
    }

    private URL createTriplestore(String name) {
        FormBody createBody = createRepositoryFormBody(name, defaultType, defaultIndexes);

        execute(httpClient, "POST", createUrl().toString(), emptyMap(), createBody,
                (res) -> {
                    if (res.code() > 399) {
                        throw new RuntimeException(
                                String.format(ERR_CREATE_FAILURE + "  Received '%s %s' from a %s request to %s",
                                        defaultType, name, res.code(), res.message(), "POST", createUrl()));
                    }
                },
                null);

        return repositoryUrl(name);
    }

    private URL clearTriplestore(String name) {
        execute(httpClient, "DELETE", statementsUrl(name).toString(), emptyMap(),null,
                (res) -> {
                    if (res.code() != 204) {
                        throw new RuntimeException(
                                String.format(ERR_DELETE_FAILURE + " Received '%s %s' from a %s request to %s",
                                        defaultType, name, res.code(), res.message(), "DELETE", repositoryUrl(name))
                        );
                    }
                },
                null);

        return repositoryUrl(name);
    }

    private URL removeTriplestore(String name) {
        execute(httpClient, "DELETE", repositoryUrl(name).toString(), emptyMap(),null,
                (res) -> {
                    if (res.code() != 204) {
                        throw new RuntimeException(
                                String.format(ERR_DELETE_FAILURE + " Received '%s %s' from a %s request to %s",
                                        defaultType, name, res.code(), res.message(), "DELETE", repositoryUrl(name))
                        );
                    }
                },
                null);

        return repositoryUrl(name);
    }

    /**
     * Composes the form that is submitted to the RDF4J Workbench web application for the creation of a RDF4J HTTP
     * repository.
     *
     * @param name the name of the repository
     * @param type the type of the repository
     * @param indexes a comma-separated list of indexes to be created for the repository
     * @return the form body to be submitted to the RDF4J Workbench
     */
    private FormBody createRepositoryFormBody(String name, String type, String indexes) {
        return new FormBody.Builder()
                .add("type", type)
                .add("Repository ID", name)
                .add("Repository title", String.format("RMap Repository defaultName: %s defaultType: %s", name, type))
                .add("Triple indexes", indexes).build();
    }

    /**
     * Returns the URL that accepts a POST for creating the RDF4J HTTP repository.
     *
     * @return the URL for creating a repository
     */
    private URL createUrl() {
        try {
            return new URL(String.format("%s/%s", workbenchBaseUrl, CREATE_URL_FRAGMENT));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Returns the URL of a repository based on its name.  The URL is not guaranteed to exist at the time this method
     * is called.  It needs to be {@link #createTriplestore() created} first.
     *
     * @param name the name of a repository, which may or may not exist
     * @return the URL to the repository, which may or may not exist
     */
    private URL repositoryUrl(String name) {
        try {
            return new URL(String.format("%s/%s", repositoryBaseUrl, String.format(REPOSITORY_URL_FRAGMENT, name)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Returns the URL used to query a RDF4J HTTP repository for statements.  The URL is not guaranteed to exist at the
     * time this method is called.  The repostiory needs to be {@link #createTriplestore() created} first.
     *
     * @param name the name of a repository, which may or may not exist
     * @return the URL to the 'statements' endpoint of the repository, which may or may not exist
     */
    private URL statementsUrl(String name) {
        try {
            return new URL(String.format("%s/%s/statements", repositoryBaseUrl,
                    String.format(REPOSITORY_URL_FRAGMENT, name)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Executes an HTTP query using the supplied client.  A response handler ought to be supplied.  An exception handler
     * is optional.
     *
     * @param client the OkHttp client used to execute the request
     * @param method the request method
     * @param url the url for the request
     * @param headers any headers for the request, optional
     * @param requestBody the request body, optional
     * @param responseHandler the response handler, suggested, but optional
     * @param exceptionHandler the exception handler, optional
     */
    private void execute(OkHttpClient client, String method, String url, Map<String, String> headers,
                         RequestBody requestBody, Consumer<Response> responseHandler,
                         Consumer<Throwable> exceptionHandler) {

        if (client == null) {
            throw new IllegalArgumentException("Http client must not be empty or null.");
        }

        client = client.newBuilder()
                .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .build();

        final Request.Builder builder = new Request.Builder();

        if (url == null || url.trim().length() == 0) {
            throw new IllegalArgumentException("Url must not be empty or null.");
        }

        builder.url(url);


        if (headers != null && !headers.isEmpty()) {
            headers.entrySet().forEach((entry) -> builder.addHeader(entry.getKey(), entry.getValue()));
        }

        Request req = null;

        switch (method) {
            case "GET":
                {
                    req = builder.get().build();
                    break;
                }

            case "DELETE":
                {
                    if (requestBody != null) {
                        req = builder.delete(requestBody).build();
                    } else {
                        req = builder.delete().build();
                    }
                    break;
                }

            case "POST":
                {
                    if (requestBody != null) {
                        req = builder.post(requestBody).build();
                    } else {
                        throw new IllegalArgumentException("POST request body must not be null!");
                    }
                }
                break;

            case "HEAD":
                {
                    req = builder.head().build();
                    break;
                }

            default:
                throw new IllegalArgumentException("Unsupported HTTP method '" + method + "'");
        }

        try (Response res = client.newCall(req).execute()) {
            if (responseHandler != null) {
                responseHandler.accept(res);
            }
        } catch (IOException e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            } else {
                throw new RuntimeException(String.format(ERR_REQUEST_FAILURE, method, url, e.getMessage()), e);
            }
        }
    }

    public String getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getDefaultIndexes() {
        return defaultIndexes;
    }

    public void setDefaultIndexes(String defaultIndexes) {
        this.defaultIndexes = defaultIndexes;
    }

    public URL getWorkbenchBaseUrl() {
        return workbenchBaseUrl;
    }

    public void setWorkbenchBaseUrl(URL workbenchBaseUrl) {
        this.workbenchBaseUrl = workbenchBaseUrl;
    }

    public URL getRepositoryBaseUrl() {
        return repositoryBaseUrl;
    }

    public void setRepositoryBaseUrl(URL repositoryBaseUrl) {
        this.repositoryBaseUrl = repositoryBaseUrl;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        if (readTimeoutMs < 1) {
            throw new IllegalArgumentException("Read timeout must be a positive integer");
        }
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        if (connectTimeoutMs < 1) {
            throw new IllegalArgumentException("Connect timeout must be a postive integer");
        }
        this.connectTimeoutMs = connectTimeoutMs;
    }

}
