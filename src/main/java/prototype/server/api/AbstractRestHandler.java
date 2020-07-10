package prototype.server.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractRestHandler {

	/**
	 * Jackson object mapper used to initialize repositories from JSON files if no initialization was done before (DB is
	 * empty).
	 */
	protected static final ObjectMapper mapper = new ObjectMapper();
	
	
	
	
}
