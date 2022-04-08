package example;
// tag::import[]
// Import all relevant classes from neo4j-java-driver dependency
import org.neo4j.driver.*;
// end::import[]

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Index {
/**
 * Example Authentication token.
 *
 * You can use `AuthTokens.basic` to create a token.  A basic token accepts a
 * Username and Password
 */
 // tag::credentials[]
  static String username = "neo4j";
  static String password = "letmein!";
// end::credentials[]
 
// tag::auth[]
    AuthToken authenticationToken = AuthTokens.basic(username, password);
// end::auth[]

/*
 * Here is the pseudocode for creating the Driver:

// tag::pseudo[]
var driver = GraphDatabase.driver(
  connectionString, // <1>
  authenticationToken, // <2>
  configuration // <3>
)
// end::pseudo[]

The first argument is the connection string, it is constructed like so:

// tag::connection[]
  address of server
          ↓
neo4j://localhost:7687
  ↑                ↑
scheme        port number
// end::connection[]
*/

/**
 * The following code creates an instance of the Neo4j Driver
 */
// tag::driver[]
// Create a new Driver instance
   static Driver driver = GraphDatabase.driver("neo4j://localhost:7687",
            AuthTokens.basic(username, password));
// end::driver[]

// tag::configuration[]
    Config config = Config.builder()
            .withConnectionTimeout(30, TimeUnit.SECONDS)
            .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
            .withMaxConnectionPoolSize(10)
            .withConnectionAcquisitionTimeout(20, TimeUnit.SECONDS)
            .withFetchSize(1000)
            .withDriverMetrics()
            .withLogging(Logging.console(Level.INFO))
            .build();
// end::configuration[]

/**
 * It is considered best practise to inject an instance of the driver.
 * This way the object can be mocked within unit tests
 */
    public static class MyService {
      private final Driver driver;

      public MyService(Driver driver) {
        this.driver = driver;
      }

      public void method() {
        // tag::session[]
        // Open a new session
        try (var session = driver.session()) {

          // Do something with the session...

          // Close the session automatically in try-with-resources block
        }
        // end::session[]
      }
    }

/**
 * These functions are wrapped in an `async` function so that we can use the await
 * keyword rather than the Promise API.
 */
    public static void main () {
    // tag::verifyConnectivity[]
    // Verify the connection details
    driver.verifyConnectivity();
    // end::verifyConnectivity[]

    System.out.println("Connection verified!");

    // tag::driver.session[]
    // Open a new session
    var session = driver.session();
    // end::driver.session[]

    // tag::session.run[]
    var query = "MATCH () RETURN count(*) AS count";
    var params = Values.parameters();

    // Run a query in an auto-commit transaction
    var res = session.run(query, params).single().get("count").asLong();
    // end::session.run[]

    System.out.println(res);

    // tag::session.close[]
    // Close the session
    session.close();
    // end::session.close[]

    new MyService(driver).method();

    driver.close();
  }
private static void showReadTransaction (Driver driver){
    try (var session = driver.session()) {

      // tag::session.readTransaction[]
      // Run a query within a Read Transaction
      var res = session.readTransaction(tx -> {
      return tx.run("""
                      MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
                      WHERE m.title = $title // <1>
                      RETURN p.name AS name
                      LIMIT 10
                      """,
              Values.parameters("title", "Arthur") // <2>
      ).list(r -> r.get("name").asString());
      // end::session.readTransaction[]
  });

    }
  }

private static void showWriteTransaction (Driver driver){
    try (var session = driver.session()) {

      // tag::session.writeTransaction[]
      session.writeTransaction(tx -> {
        return tx.run(
                "CREATE (p:Person {name: $name})",
                Values.parameters("name", "Michael")).consume();
      });
      // end::session.writeTransaction[]
    }
}

private static void showManualTransaction(Driver driver){
    // tag::session.beginTransaction[]
    // Open a new session
    var session = driver.session(
            SessionConfig.builder()
                    .withDefaultAccessMode(AccessMode.WRITE)
                    .build());

    // Manually create a transaction
    var tx = session.beginTransaction();
    // end::session.beginTransaction[]

    var query = "MATCH (n) RETURN count(n) AS count";
    var params = Values.parameters();

    // tag::session.beginTransaction.Try[]
    try {
      // Perform an action
      tx.run(query, params);

      // Commit the transaction
      tx.commit();
    } catch (Exception e) {
      // If something went wrong, rollback the transaction
      tx.rollback();
    }
    // end::session.beginTransaction.Try[]

    session.close();
  }

/**
 * This is an example function that will create a new Person node within
 * a read transaction and return the properties for the node.
 *
 * @param {string} name
 * @return {Record<string, any>}  The properties for the node
 */
// tag::createPerson[]
private static Map<String,Object> createPerson(String name) {
    // tag::sessionWithArgs[]
    // Create a Session for the `people` database
    var sessionConfig = SessionConfig.builder()
            .withDefaultAccessMode(AccessMode.WRITE)
            .withDatabase("people")
            .build();

    try (var session = driver.session(sessionConfig)) {
        // end::sessionWithArgs[]

        // Create a node within a write transaction
        var res = session.writeTransaction(tx ->
                tx.run("CREATE (p:Person {name: $name}) RETURN p",
                                Values.parameters("name", name))
                        .single());

        // Get the `p` value from the first record
        var p = res.get("p").asNode();

        // Return the properties of the node
        return p.asMap();
        // Autoclose the sesssion
    }
}
// end::createPerson[]

// Run the main method above
// main()
}
