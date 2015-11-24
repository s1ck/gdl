[![Build Status](https://travis-ci.org/s1ck/gdl.svg?branch=master)](https://travis-ci.org/s1ck/gdl)

## GDL - Graph Definition Language

Inspired by the popular graph query language [Cypher](http://neo4j.com/docs/stable/cypher-query-lang.html),
which is implemented in [Neo4j](http://neo4j.com/), I started developing an [ANTLR](http://www.antlr.org/)
grammar to define property graphs. I added the concept of subgraphs into the language to support multiple, 
possible overlapping property graphs in one database. For me, this project is a way to learn more about 
ANTLR and context-free grammars.

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
(alice:User {name = "Alice", age = 23})
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
(alice)-[e1:knows {since = 2014}]->(bob)
```

Define multiple outgoing edges from the same source vertex (i.e. `alice`):

```
(alice)-[e1:knows {since = 2014}]->(bob)
(alice)-[e2:knows {since = 2013}]->(eve)
```

Define paths (four vertices and three edges are created):

```
()-->()<--()-->()
```

Define a graph (must contain at least one vertex):

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
g:Community {title = "Graphs", memberCount = 42}[()]
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
g1:Community {title = "Graphs", memberCount = 23}[
    (alice:User)
    (bob:User)
    (eve:User)
]
g2:Community {title = "Databases", memberCount = 42}[
    (alice)
]
g2:Community {title = "Hadoop", memberCount = 31}[
    (bob)
    (eve)
]
```

Define three graphs with overlapping vertex and edge sets (`e` is in `g1` and `g2`):

```
g1:Community {title = "Graphs", memberCount = 23}[
    (alice:User)-[:knows]->(bob:User)
    (bob)-[e:knows]->(eve:User)
    (eve)
]
g2:Community {title = "Databases", memberCount = 42}[
    (alice)
]
g2:Community {title = "Hadoop", memberCount = 31}[
    (bob)-[e]->(eve)
]
```

## Usage examples

Create a database from a GDL string:

```java
GDLHandler handler = new GDLHandler.Builder().buildFromString("g[(alice)-[e1:knows {since = 2014}]->(bob)]");

for (Vertex v : handler.getVertices()) {
    // do something
}

// access elements by variable
Graph g = handler.getGraphCache().get("g");
Vertex alice = handler.getVertexCache().get("alice");
Edge e = handler.getEdgeCache().get("e1");
```

Create a database from an `InputStream` or an input file:

```java
GDLHandler handler1 = new GDLHandler.Builder().buildFromStream(stream);
GDLHandler handler2 = new GDLHandler.Builder().buildFromFile(fileName);
```

Append data to a given handler:

```java
GDLHandler handler = new GDLHandler.Builder().buildFromString("g[(alice)-[e1:knows {since = 2014}]->(bob)]");

handler.append("g[(alice)-[:knows]->(eve)]");
```

## License

Licensed under the [GNU General Public License, v3](http://www.gnu.org/licenses/gpl-3.0.html).
