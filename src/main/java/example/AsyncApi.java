package example;
// tag::import[]
// Import all relevant classes from neo4j-java-driver dependency
import neoflix.AppUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.neo4j.driver.*;
import org.neo4j.driver.reactive.RxSession;
// end::import[]

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AsyncApi {

    static {
// Load config from .env
        AppUtils.loadProperties();
    }

    // Load Driver
    static Driver driver = GraphDatabase.driver(System.getProperty("NEO4J_URI"),
            AuthTokens.basic(System.getProperty("NEO4J_USERNAME"), System.getProperty("NEO4J_PASSWORD")));

    static void syncExample() {
        // tag::sync[]
        try (var session = driver.session()) {

            var res = session.readTransaction(tx -> tx.run(
                    "MATCH (p:Person) RETURN p.name AS name LIMIT 10").list());
            res.stream()
                    .map(row -> row.get("name"))
                    .forEach(System.out::println);
        } catch (Exception e) {
            // There was a problem with the
            // database connection or the query
            e.printStackTrace();
        }
        // end::sync[]
    }

    static void asyncExample() {
        // tag::async[]
        var session = driver.asyncSession();
        session.readTransactionAsync(tx -> tx.runAsync(
                        "MATCH (p:Person) RETURN p.name AS name LIMIT 10")

                .thenApplyAsync(res -> res.listAsync(row -> row.get("name")))
                .thenAcceptAsync(System.out::println)
                .exceptionallyAsync(e -> {
                    e.printStackTrace();
                    return null;
                })
        );
        // end::async[]
    }

    static void reactiveExample() {
        // tag::reactive[]
        Flux.usingWhen(Mono.fromSupplier(driver::rxSession),
            session -> session.readTransaction(tx -> {
                var rxResult = tx.run(
                        "MATCH (p:Person) RETURN p.name AS name LIMIT 10");
                return Flux
                    .from(rxResult.records())
                    .map(r -> r.get("name").asString())
                    .doOnNext(System.out::println)
                    .then(Mono.from(rxResult.consume()));
            }
            ), RxSession::close);
        // end::reactive[]
    }
}