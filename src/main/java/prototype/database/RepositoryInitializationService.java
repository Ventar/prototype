package prototype.database;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initialization service for the repositories. During the system initialization
 * the base data has to be loaded from the attached JSON files. To do so we need
 * to ensure an ordering to avoid the creation of wrong data in the database.
 * 
 * @author Michael Rodenbuecher
 * @since 2020-07-10
 *
 */
@Service
public class RepositoryInitializationService {

	public static final Logger LOG = LoggerFactory.getLogger(RepositoryInitializationService.class);

	/**
	 * The user repository.
	 */
	@Autowired
	private UserRepository userRepo;

	/**
	 * Listener for the life cycle events of the IoC framework. This listener is
	 * called when then IoC was started or refreshed. In contrast to the
	 * {@link PostConstruct} the method is invoked when the complete initialization
	 * of services was done and NOT only the dependency injection. This allows
	 * transactional support for database transactions out of the spring transaction
	 * manager.
	 * 
	 * @param event the event that was fired by the IoC
	 */
	@EventListener(ContextRefreshedEvent.class)
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.debug("Received ContextRefreshedEvent in repository ::= [{}]", getClass().getSimpleName());
		try {
			userRepo.importData();
		} catch (Exception e) {
			LOG.error("Could not initialize repositories.");
		}
	}

}
