package prototype.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Servlet to generate the Open API 3.0 specification for the REST API.
 * 
 * @author Michael Rodenbuecher
 * @since 2020-02-20
 *
 */
public class OpenAPIServlet extends HttpServlet {

	private static final long serialVersionUID = -2181645734283829181L;

	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_YAML = "application/yaml";
	public static final String ACCEPT_HEADER = "Accept";

	private OpenApiContext openApiContext;

	@SuppressWarnings("rawtypes")
	@Override
	public void init(ServletConfig config) throws ServletException {

		OpenAPI oas = new OpenAPI();
		Info info = new Info().title("Prototype Module Version 0.0.1").description("Prototype for a module.")
				.contact(new Contact().email("michael.rodenbuecher@atos.net"))
				.license(new License().name("MIT License ")).version("0.0.1");

		oas.info(info);
		oas.addServersItem(new Server().url("http://localhost/api"));

		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas)
				.resourcePackages(Stream.of("prototype.server").collect(Collectors.toSet()));

		try {
			openApiContext = new JaxrsOpenApiContextBuilder().servletConfig(config).openApiConfiguration(oasConfig)
					.buildContext(true);
		} catch (OpenApiConfigurationException e) {
			throw new ServletException(e.getMessage(), e);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		OpenAPI oas = openApiContext.read();

		String type = "json";

		String acceptHeader = req.getHeader(ACCEPT_HEADER);
		if (!StringUtils.isBlank(acceptHeader) && acceptHeader.toLowerCase().contains(APPLICATION_YAML)) {
			type = "yaml";
		} else {
			// check URL:
			if (req.getRequestURL().toString().toLowerCase().endsWith("yaml")) {
				type = "yaml";
			}
		}

		resp.setStatus(200);

		if (type.equalsIgnoreCase("yaml")) {
			resp.setContentType(APPLICATION_YAML);
			PrintWriter pw = resp.getWriter();
			pw.write(Yaml.pretty(oas));
			pw.close();
		} else {
			resp.setContentType(APPLICATION_JSON);
			PrintWriter pw = resp.getWriter();
			pw.write(Json.pretty(oas));
			pw.close();
		}

	}
}
