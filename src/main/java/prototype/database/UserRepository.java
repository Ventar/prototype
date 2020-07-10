package prototype.database;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import prototype.entities.User;

@Repository
public class UserRepository extends AbstractRepository<User> {

	public static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

	@Override
	protected void importData() throws Exception {
		List<User> user = loadFromJSON("/data/user.json");
		for (User u : user) {
			create(u);
		}

	}

	/**
	 * Returns the user with the given email
	 * 
	 * @param email the email
	 * @return the user if it exist, <code>null</code> otherwise
	 */
	@Transactional
	public User getUserByEMail(String email) {

		Query<User> q = sessionFactory.getCurrentSession().createNamedQuery("User.byEMail", User.class)
				.setParameter("email", email);

		try {
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}


}
