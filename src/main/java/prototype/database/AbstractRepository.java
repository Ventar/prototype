package prototype.database;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.reflect.TypeToken;

import prototype.entities.AbstractEntity;

/**
 * Base class for all repositories that stores game data within the database.
 * 
 * @author Michael Rodenbuecher
 * @since 2020-07-10
 *
 */
public abstract class AbstractRepository<T extends AbstractEntity> {

	public static final Logger LOG = LoggerFactory.getLogger(AbstractRepository.class);

	/**
	 * Jackson object mapper used to initialize repositories from JSON files if no
	 * initialization was done before (DB is empty).
	 */
	protected static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Hibernate session factory.
	 */
	@Autowired
	protected SessionFactory sessionFactory;

	/**
	 * The class of the primary entity that is managed by this repository.
	 */
	protected Class<T> clazz;

	/**
	 * Creates a new instance of a game data repository.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public AbstractRepository() {
		super();

		// this part of the code is based on Googles Guava framework and allows to
		// resolve the class of the parameterized type T
		// of this class. The class itself is needed for the hibernate query creation in
		// the common data query methods.

		TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
		this.clazz = (Class<T>) typeToken.getRawType();

	}

	/**
	 * Load the basic data elements for this entity from the JSON file in the
	 * classpath.
	 * 
	 * @param filename the JSON filename
	 * @return the entities
	 * 
	 */
	public List<T> loadFromJSON(String filename) throws JsonParseException, JsonMappingException, IOException {

		TypeFactory typeFactory = mapper.getTypeFactory();
		JavaType type = typeFactory.constructParametricType(List.class, clazz);

		return mapper.readValue(getClass().getResourceAsStream(filename), type);
	}

	/**
	 * Performs the initialization of the repository. This method is called when all
	 * beans were initialized or when the context of the IoC was refreshed. The
	 * implementing class has to ensure that all initialization data is written to
	 * the database if the database was not initialized before. IF the database was
	 * initialized the data should NOT be overwritten by this method. The method
	 * itself is called when transactional support for the IoC container is
	 * available.<br>
	 * This method is called by the
	 * {@link AbstractRepository#onApplicationEvent(ContextRefreshedEvent)} method
	 * and was introduced to abstract the container life cycle from the DB
	 * repository management.
	 */
	@Transactional
	protected abstract void importData() throws Exception;

	/**
	 * Creates a new entity in the database
	 * 
	 * @param t the entity
	 * @return the new entity with a generated primary key
	 */
	@Transactional
	public T create(T t) {
		LOG.trace("Entity to create::= [{}]", t);
		Serializable pk = sessionFactory.getCurrentSession().save(t);
		T entity = sessionFactory.getCurrentSession().get(clazz, pk);
		LOG.debug("Created new entity ::= [{}]", entity);

		return entity;
	}

	/**
	 * Merges the passed entity to the database
	 * 
	 * @param t the entity
	 * @return the merged entity
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public T merge(T t) {
		LOG.trace("Entity to update ::= [{}]", t);
		T entity = (T) sessionFactory.getCurrentSession().merge(t);
		LOG.debug("Updated entity ::= [{}]", entity);
		return entity;
	}

	/**
	 * Returns all entities of the managed entity type.
	 * 
	 * @return all entities
	 */
	@Transactional(readOnly = true)
	public List<T> getAll() {
		Query<T> q = sessionFactory.getCurrentSession().createNamedQuery(clazz.getSimpleName() + ".all", clazz);
		return q.getResultList();
	}

}
