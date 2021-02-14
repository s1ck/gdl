[![Build Status](https://github.com/s1ck/gdl/workflows/Java%20CI/badge.svg)](https://github.com/s1ck/gdl/actions?workflow=Java+CI)

## GDL - Graph Definition Language

Inspired by the popular graph query language [Cypher](http://neo4j.com/docs/stable/cypher-query-lang.html),
which is implemented in [Neo4j](http://neo4j.com/), I started developing an [ANTLR](http://www.antlr.org/)
grammar to define property graphs. I added the concept of subgraphs into the language to support multiple, 
possible overlapping property graphs in one database. 

For me, this project is a way to learn more about ANTLR and context-free grammars. Furthermore, GDL is used
for unit testing and graph definition in [Gradoop](https://github.com/dbs-leipzig/gradoop), a framework for 
distributed graph analytics.

The project contains the grammar and a listener implementation which transforms GDL scripts into
property graph model elements (i.e. graphs, vertices and edges).

## Data model

The data model contains three elements: graphs, vertices and edges. Any element has an optional
label and can have multiple attributes in the form of key-value pairs. Vertices and edges may 
be contained in an arbitrary number of graphs including zero graphs. Edges are binary and directed.

## Language Examples

Define a vertex:

```
()
```

Define a vertex and assign it to variable `alice`:

```
(alice)
```

Define a vertex with label `User`:

```
(:User)
```

Define a vertex with label `User`, assign it to variable `alice` and give it some properties:

```
(alice:User {name : "Alice", age : 23})
```

Property values can also be null:

```
(alice:User {name : "Alice", age : 23, city : NULL})
```

Numeric property values can have specific data types:

```
(alice:User {name : "Alice", age : 23L, height : 1.82f, weight : 42.7d})
```

Property values can also be ordered lists:

```
(alice:User {name : "Alice", age : 23, codes: ["Java", "Rust", "Scala"]})
```

Define an outgoing edge:

```
(alice)-->()
```

Define an incoming edge:

```
(alice)<--()
```

Define an edge with label `knows`, assign it to variable `e1` and give it some properties:

```
(alice)-[e1:knows {since : 2014}]->(bob)
```

Define multiple outgoing edges from the same source vertex (i.e. `alice`):

```
(alice)-[e1:knows {since : 2014}]->(bob)
(alice)-[e2:knows {since : 2013}]->(eve)
```

Define paths (four vertices and three edges are created):

```
()-->()<--()-->()
```

Define a graph with one vertex (graphs can be empty):

```
[()]
```

Define a graph and assign it to variable `g`:

```
g[()]
```

Define a graph with label `Community`:

```
:Community[()]
```

Define a graph with label `Community`, assign it to variable `g` and give it some properties:

```
g:Community {title : "Graphs", memberCount : 42}[()]
```

Define mixed path and graph statements (elements in the paths don't belong to a specific graph):

```
()-->()<--()-->()
[()]
```

Define a fragmented graph with variable reuse:

```
g[(a)-->()]
g[(a)-->(b)]
g[(b)-->(c)]
```

Define three graphs with overlapping vertex sets (e.g. `alice` is in `g1` and `g2`):

```
g1:Community {title : "Graphs", memberCount : 23}[
    (alice:User)
    (bob:User)
    (eve:User)
]
g2:Community {title : "Databases", memberCount : 42}[
    (alice)
]
g2:Community {title : "Hadoop", memberCount : 31}[
    (bob)
    (eve)
]
```

Define three graphs with overlapping vertex and edge sets (`e` is in `g1` and `g2`):

```
g1:Community {title : "Graphs", memberCount : 23}[
    (alice:User)-[:knows]->(bob:User),
    (bob)-[e:knows]->(eve:User),
    (eve)
]
g2:Community {title : "Databases", memberCount : 42}[
    (alice)
]
g2:Community {title : "Hadoop", memberCount : 31}[
    (bob)-[e]->(eve)
]
```

### Query Expressions

As part of his thesis, [Max](https://github.com/DarthMax) extended the grammar to support `MATCH .. WHERE ..`
statements analogous to Cypher. Besides defining a graph it is now also possible to formulate a query including 
patterns, variable length paths and predicates:

```
MATCH (alice:Person)-[:knows]->(bob:Person)-[:knows*2..2]->(eve:Person)
WHERE (alice.name = "Alice" AND bob.name = "Bob") 
OR (alice.age > bob.age)
OR (alice.age > eve.age)
```

**Note** that queries always start with the `MATCH` keyword optionally followed by one or more
`WHERE` clauses. 

## Usage examples

Add dependency to your maven project:

```
<dependency>
    <groupId>com.github.s1ck</groupId>
    <artifactId>gdl</artifactId>
    <version>0.3.7</version>
</dependency>
```

Create a database from a GDL string:

```java
GDLHandler handler = new GDLHandler.Builder().buildFromString("g[(alice)-[e1:knows {since : 2014}]->(bob)]");

for (Vertex v : handler.getVertices()) {
    // do something
}

// access elements by variable
Graph g = handler.getGraphCache().get("g");
Vertex alice = handler.getVertexCache().get("alice");
Edge e = handler.getEdgeCache().get("e1");
```

Read predicates from a Cypher query:

```java
GDLHandler handler = new GDLHandler.Builder().buildFromString("MATCH (a:Person)-[e:knows]->(b:Person) WHERE a.age > b.age");

// prints (((a.age > b.age AND a.__label__ = Person) AND b.__label__ = Person) AND e.__label__ = knows)
handler.getPredicates().ifPresent(System.out::println);
```

Create a database from an `InputStream` or an input file:

```java
GDLHandler handler1 = new GDLHandler.Builder().buildFromStream(stream);
GDLHandler handler2 = new GDLHandler.Builder().buildFromFile(fileName);
```

Append data to a given handler:

```java
GDLHandler handler = new GDLHandler.Builder().buildFromString("g[(alice)-[e1:knows {since : 2014}]->(bob)]");

handler.append("g[(alice)-[:knows]->(eve)]");
```

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
