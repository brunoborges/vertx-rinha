package vertx.rinha;

import java.io.File;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import vertx.model.Message;

public class RinhaApp extends AbstractVerticle implements Handler<HttpServerRequest> {

    static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RinhaApp.class);

    static final String PATH_PESSOAS = "/pessoas";
    static final String PATH_PESSOA = "/pessoas/:id";
    static final String PATH_CONTAGEM = "/contagem-pessoas";

    private static final CharSequence RESPONSE_TYPE_PLAIN = HttpHeaders.createOptimized("text/plain");
    private static final CharSequence RESPONSE_TYPE_JSON = HttpHeaders.createOptimized("application/json");

    private static final CharSequence HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("Content-Type");
    private static final CharSequence HEADER_CONTENT_LENGTH = HttpHeaders.createOptimized("Content-Length");

    static {
        System.setProperty("vertx.disableMetrics", "true");
        System.setProperty("vertx.disableH2c", "true");
        System.setProperty("vertx.disableWebsockets", "true");
        System.setProperty("vertx.flashPolicyHandler", "false");
        System.setProperty("vertx.threadChecks", "false");
        System.setProperty("vertx.disableContextTimings", "true");
        System.setProperty("vertx.disableTCCL", "true");
        System.setProperty("vertx.disableHttpHeadersValidation", "true");
        System.setProperty("vertx.eventLoopPoolSize", "1");
        System.setProperty("io.netty.buffer.checkBounds", "false");
        System.setProperty("io.netty.buffer.checkAccessible", "false");
    }

    private HttpServer server;

    private Database database = new Database();

    @Override
    public void handle(HttpServerRequest event) {
        logger.info("Request event path: " + event.path());
        try {
            switch (event.path()) {
                case PATH_PESSOAS:
                    handlePessoas(event);
                    break;
                // case PATH_PESSOA:
                // handlePessoa(event);
                // break;
                case PATH_CONTAGEM:
                    handleContagem(event);
                    break;
                default:
                    handleJson(event);
                    break;
            }
        } catch (Exception e) {
            sendError(event, e);
        }
    }

    private void handleContagem(HttpServerRequest event) {
        event.response().end(Integer.toString(database.count()));
    }

    private void handlePostPessoa(HttpServerRequest event) {
        event.bodyHandler(buffer -> {
            Pessoa pessoa = new Pessoa();
            pessoa.fromJsonObject(buffer.toJsonObject());
            database.save(pessoa);
            HttpServerResponse response = event.response();
            MultiMap headers = response.headers();
            headers.add(HEADER_CONTENT_TYPE, RESPONSE_TYPE_JSON);
            response.end(new JsonObject(Json.encode(pessoa)).toBuffer());
        });
    }

    private void handlePessoas(HttpServerRequest event) {
        if (event.method().equals(HttpMethod.GET)) {
            handleListPessoas(event);
        } else if (event.method().equals(HttpMethod.POST)) {
            handlePostPessoa(event);
        }
    }

    private void handleListPessoas(HttpServerRequest event) {
        HttpServerResponse response = event.response();
        MultiMap headers = response.headers();
        headers.add(HEADER_CONTENT_TYPE, RESPONSE_TYPE_JSON);

        JsonArray array = new JsonArray(database.find(event.getParam("t")));
        response.end(array.toBuffer());

    }

    private void sendError(HttpServerRequest req, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        req.response().setStatusCode(500).end();
    }

    private void handleJson(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        MultiMap headers = response.headers();
        headers.add(HEADER_CONTENT_TYPE, RESPONSE_TYPE_JSON);
        response.end(new Message("Hello, World!").toBuffer());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        int port = 8080;
        server = vertx.createHttpServer(new HttpServerOptions()).requestHandler(RinhaApp.this);
        JsonObject config = config();

        /*
         * PgConnectOptions options = new PgConnectOptions();
         * options.setDatabase(config.getString("database", "hello_world"));
         * options.setHost(config.getString("host", "tfb-database"));
         * options.setPort(config.getInteger("port", 5432));
         * options.setUser(config.getString("username", "benchmarkdbuser"));
         * options.setPassword(config.getString("password", "benchmarkdbpass"));
         * options.setCachePreparedStatements(true);
         * options.setPipeliningLimit(100_000); // Large pipelining means less flushing
         * and we use a single connection
         * // anyway
         * 
         * PgConnection.connect(vertx, options)
         * .flatMap(conn -> {
         * client = (SqlClientInternal) conn;
         * Future<PreparedStatement> f1 = conn.prepare(SELECT_WORLD)
         * .andThen(onSuccess(ps -> SELECT_WORLD_QUERY = ps.query()));
         * Future<PreparedStatement> f2 = conn.prepare(SELECT_FORTUNE)
         * .andThen(onSuccess(ps -> SELECT_FORTUNE_QUERY = ps.query()));
         * Future<PreparedStatement> f3 = conn.prepare(UPDATE_WORLD)
         * .andThen(onSuccess(ps -> UPDATE_WORLD_QUERY = ps.query()));
         * Future<WorldCache> f4 = conn.preparedQuery(SELECT_WORLDS)
         * .collecting(Collectors.mapping(row -> new CachedWorld(row.getInteger(0),
         * row.getInteger(1)),
         * Collectors.toList()))
         * .execute()
         * .map(worlds -> new WorldCache(worlds.value()))
         * .andThen(onSuccess(wc -> WORLD_CACHE = wc));
         * return CompositeFuture.join(f1, f2, f3, f4);
         * })
         * .transform(ar -> {
         * databaseErr = ar.cause();
         * return server.listen(port);
         * })
         * .<Void>mapEmpty()
         * .onComplete(startPromise);
         */

        server.listen(port);
        startPromise.complete();
    }

    public static CharSequence createDateHeader() {
        return HttpHeaders.createOptimized(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
    }

    public static void main(String[] args) throws Exception {
        int eventLoopPoolSize = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
        String sizeProp = System.getProperty("vertx.eventLoopPoolSize");
        if (sizeProp != null) {
            try {
                eventLoopPoolSize = Integer.parseInt(sizeProp);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        JsonObject config = null;
        if (args.length > 0) {
            config = new JsonObject(new String(Files.readAllBytes(new File(args[0]).toPath())));
        } else {
            config = new JsonObject();
            config.put("host", "db-host");
            config.put("username", "db-user");
            config.put("password", "db-pass");
            config.put("database", "db-name");
        }

        VertxOptions options = new VertxOptions().setEventLoopPoolSize(eventLoopPoolSize)
                .setPreferNativeTransport(true);
        Vertx vertx = Vertx.vertx(options);
        vertx.exceptionHandler(err -> err.printStackTrace());

        DeploymentOptions deployOptions = new DeploymentOptions().setInstances(eventLoopPoolSize).setConfig(config);
        vertx.deployVerticle(RinhaApp.class.getName(),
                deployOptions, event -> {
                    if (event.succeeded()) {
                        logger.info("Server listening on port " + 8080);
                    } else {
                        logger.error("Unable to start your application", event.cause());
                    }
                });
    }

}
