package prototype.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Plain data class for a USER.
 * 
 * @author Michael Rodenbuecher
 * @since 2020-07-10
 *
 */
@Entity
@Table(name = "USER")
@NamedQueries(

{ @NamedQuery(name = "User.all", query = "SELECT x FROM User x"),
		@NamedQuery(name = "User.byEMail", query = "SELECT x FROM User x WHERE x.email = :email") })
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends AbstractEntity {

	@Column(name = "FIRST_NAME")
	public String firstName;

	@Column(name = "LAST_NAME")
	public String lastName;

	@Column(name = "EMAIL")
	public String email;

	@Override
	public String toString() {
		return "User [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + "]";
	}

}
