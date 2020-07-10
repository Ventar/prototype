package prototype.server;

import java.util.EnumSet;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.DispatcherType;
import javax.ws.rs.Path;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class EmbeddedJettyServer {

	public static final Logger LOG = LoggerFactory.getLogger(EmbeddedJettyServer.class);

	@Autowired
	private ApplicationContext appCtx;

	@PostConstruct
	public void postConstruct() {
		try {

			Server server = new Server();

			// builds a list of multiple handlers. If handler one cannot handle the request
			// handler two will pickup. The static
			// content handler is the first one, i.e. if no file is found (for /api that
			// will never happen because we do not have a
			// corresponding sub folder) the request is forwarded to the next handler.
			HandlerList handlerList = new HandlerList();
			handlerList.addHandler(createResourceHandler());
			handlerList.addHandler(createJerseyHandler());

			server.setHandler(handlerList);
			server.setConnectors(createConnectors(server));
			server.start();

			LOG.info("Started embedded Jetty server...");
		} catch (Exception e) {
			LOG.error("Cannot start embedded Jetty server: ", e);
		}
	}

	/**
	 * Creates a handler to serve static resource requests from the /webapp
	 * directory of the JAR file.
	 * 
	 * @return the handler
	 */
	private Handler createResourceHandler() {
		// serves static files from the 7webapp resource path
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setBaseResource(Resource.newClassPathResource("/webapp"));
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		return resourceHandler;
	}

	/**
	 * Creates the handler to handle the REST requests that are processed by Jersey
	 * 
	 * @return the handler
	 */
	private Handler createJerseyHandler() {
		ServletContextHandler jerseyHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		jerseyHandler.setContextPath("/");

		// enable CORS support for the API to use it with Swagger Editor / Swagger UI
		FilterHolder cors = jerseyHandler.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
		cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");

		// Jersey config
		// -----------------------------------------------------------------------------------------------
		ResourceConfig jerseyConfig = new ResourceConfig();
		Map<String, Object> paths = appCtx.getBeansWithAnnotation(Path.class);

		for (Object path : paths.values()) {
			jerseyConfig.registerInstances(path);
		}

		ServletContainer jerseyServlet = new ServletContainer(jerseyConfig);
		jerseyHandler.addServlet(new ServletHolder(jerseyServlet), "/api/*");
		// the open API servlet generates the documentation of the REST API
		jerseyHandler.addServlet(new ServletHolder(new OpenAPIServlet()), "/openapi/*");

		return jerseyHandler;
	}

	/**
	 * Create the connectors for the server, i.e. the part that is responsible for
	 * the protocol handling. Both, HTTP and HTTPS, are supported where HTTPS has an
	 * untrusted self-signed certificate at the moment.
	 * 
	 * @param server the server for which the connectors are used
	 * @return the connector configuration
	 */
	private Connector[] createConnectors(Server server) {
		// enable self signed certificates for the server. Storing the password here
		// isn't the best idea but for this project it
		// works fine.

		HttpConfiguration https = new HttpConfiguration();
		https.addCustomizer(new SecureRequestCustomizer());

		SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStoreResource(Resource.newClassPathResource("/keystore.jks"));
		sslContextFactory.setKeyStorePassword("sdf56JKL!");
		sslContextFactory.setKeyManagerPassword("sdf56JKL!");

		ServerConnector sslConnector = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
		sslConnector.setPort(443);

		// for testing purposes we keep the HTTP connector

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(80);

		return new Connector[] { connector, sslConnector };
	}

}
