package example;

import neoflix.AppUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;
import org.neo4j.driver.types.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Results {
    static {
        // Load config from .env
        AppUtils.loadProperties();
    }

    // Load Driver
    static Driver driver = GraphDatabase.driver(System.getProperty("NEO4J_URI"),
            AuthTokens.basic(System.getProperty("NEO4J_USERNAME"), System.getProperty("NEO4J_PASSWORD")));


    public static void main(String[] args) {
        // Verify Connectivity
        driver.verifyConnectivity();

        try (var session = driver.session()) {

            // tag::run[]
            // Execute a query within a read transaction
            Result res = session.readTransaction(tx -> tx.run("""
                            MATCH path = (person:Person)-[actedIn:ACTED_IN]->(movie:Movie)
                            RETURN path, person, actedIn, movie,
                                   size ( (person)-[:ACTED]->() ) as movieCount,
                                   exists { (person)-[:DIRECTED]->() } as isDirector
                            LIMIT 1
                    """));
            // end::run[]

            // tag::records[]
            // Get single row, error when more/less than 1
            Record row = res.single();
            // Materialize list
            List<Record> rows = res.list();
            // Stream results
            Stream<Record> rowStream = res.stream();

            // iterate (sorry no foreach)
            while (res.hasNext()) {
                var next = res.next();
            }
            // end::records[]

            // tag::record[]
            // column names
            row.keys();
            // check for existence
            row.containsKey("movie");
            // number of columns
            row.size();
            // get a numeric value (int, long, float, double also possible)
            Number count = row.get("movieCount").asInt(0);
            // get a boolean value
            boolean isDirector = row.get("isDirector").asBoolean();
            // get node
            row.get("movie").asNode();

            // end::record[]

            // tag::get[]
            // Get a node
            Node person = row.get("person").asNode();
            // end::get[]

            // Working with node objects
            // tag::node[]
            var nodeId = person.id(); // (1)
            var labels = person.labels(); // (2)
            var properties = person.asMap(); // (3)
            // end::node[]

            // Working with Value Structures
            // tag::values[]
            Value nodeValue = row.get("movie");

            // key names
            Iterable<String> keys = nodeValue.keys();
            // number of values / length of list
            nodeValue.size();
            // get all contained values
            Iterable<Value> values = nodeValue.values();

            // treat value as type, e.g. Node, Relationship, Path or primitive
            Node node = nodeValue.asNode();

            // string-key accessors
            Value titleValue = node.get("title");
            String title = titleValue.asString();
            Number year = node.get("year").asNumber();
            Integer releaseYear = node.get("year").asInt(0);

            // index based accessors for lists and records
            nodeValue.get(0);
            // end::values[]

            // Working with relationship objects
            // tag::rel[]
            var actedIn = row.get("actedIn").asRelationship();

            var relId = actedIn.id(); // (1)
            String type = actedIn.type(); // (2)
            var relProperties = actedIn.asMap(); // (3)
            var startId = actedIn.startNodeId(); // (4)
            var endId = actedIn.endNodeId(); // (5)
            // end::rel[]

            // Working with Paths
            // tag::path[]
            Path path = row.get("path").asPath();

            Node start = path.start(); // (1)
            Node end = path.end(); // (2)
            var length = path.length(); // (3)
            Iterable<Path.Segment> segments = path; // (4)
            Iterable<Node> nodes = path.nodes(); // (5)
            Iterable<Relationship> rels = path.relationships(); // (6)
            // end::path[]

            // tag::segments[]
            path.forEach(segment -> {
                System.out.println(segment.start());
                System.out.println(segment.end());
                System.out.println(segment.relationship());
            });
            // end::segments[]

            // tag::summary[]
            ResultSummary summary = res.consume();
            // Time in milliseconds before receiving the first result
            summary.resultAvailableAfter(TimeUnit.MILLISECONDS); // 10
            // Time in milliseconds once the final result was consumed
            summary.resultConsumedAfter(TimeUnit.MILLISECONDS); // 30
            // end::summary[]

            // tag::summary:counters[]
            SummaryCounters counters = summary.counters();
            // some example counters
            // nodes and relationships
            counters.containsUpdates();
            counters.nodesCreated();
            counters.labelsAdded();
            counters.relationshipsDeleted();
            counters.propertiesSet();

            // indexes and constraints
            counters.indexesAdded();
            counters.constraintsRemoved();
            // updates to system db
            counters.containsSystemUpdates();
            counters.systemUpdates();
            // end::summary:counters[]

            // tag::summary:infra[]
            summary.query();
            summary.queryType();
            summary.notifications();
            summary.database();
            summary.server();
            // end::summary:infra[]


            // tag::summary:plan[]
            // query plan & profile
            summary.hasPlan();
            summary.plan();
            summary.hasProfile();
            summary.profile();
            // end::summary:plan[]
            /*
            // Integers
            // tag::integers[]
            // import { int, isInt } from "neo4j-driver"

            // Convert a JavaScript "number" into a Neo4j Integer
            var thisYear = int(2022);

            // Check if a value is a Neo4j integer
            System.out.println(isInt(thisYear)) // true

            // Convert the Neo4j integer into a JavaScript number
            System.out.println(thisYear.toNumber()) // 2022
            // end::integers[]
            */
            // tag::temporal[]
            // Driver consumes regular java.time.* datatypes
            // Temporal Types from Value
            var released = nodeValue.get("released");
            released.asLocalDate();
            released.asLocalDateTime();
            released.asLocalTime();
            released.asOffsetDateTime();

            // custom duration type
            IsoDuration duration = released.asIsoDuration();
            // end::temporal[]

            // tag::spatial[]
            Point loc = node.get("location").asPoint();
            // end::spatial[]

            // Close the driver
            driver.close();
        }
    }
}