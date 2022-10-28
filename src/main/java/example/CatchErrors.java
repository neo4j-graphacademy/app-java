package example;
// tag::import[]
// Import all relevant classes from neo4j-java-driver dependency
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.*;
// end::import[]

import neoflix.ValidationException;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CatchErrors {

  static String username = "neo4j";
  static String password = "letmein!";

  static Driver driver = GraphDatabase.driver("neo4j://localhost:7687",
            AuthTokens.basic(username, password));


  public static void main () {
    String email = "uniqueconstraint@example.com";
    // tag::constraint-error[]
    try (var session = driver.session()) {
      session.writeTransaction(tx -> {
          var res = tx.run("CREATE (u:User {email: $email}) RETURN u",
                           Values.parameters("email", email));
          return res.single().get('u').asMap();
      }); 
    } catch(Neo4jException e) {
      if (e.code().equals("Neo.ClientError.Schema.ConstraintValidationFailed")) {
        // System.err.println(e.getMessage()); // Node(33880) already exists with...
        throw new ValidationException("An account already exists with the email address "+email,
         Map.of("email","Email address already taken"));
      }
      throw e;
    }
    // end::constraint-error[]
  }
}