package prototype.server.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import prototype.database.UserRepository;
import prototype.entities.User;

@Path("/user")
@Produces(value = { "application/json" })
@Component
public class UnitRestHandler extends AbstractRestHandler {

	/**
	 * Repository to access user.
	 */
	@Autowired
	private UserRepository userRepo;

	@GET
	public Response getAllUser() throws JsonProcessingException {
		List<User> user = userRepo.getAll();
		return Response.status(Status.OK).entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user))
				.build();
	}

}
