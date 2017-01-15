/*
 * This file is part of GDL.
 *
 * GDL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDL.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.s1ck.gdl;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.Vertex;
import org.s1ck.gdl.model.predicates.Predicate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class that wraps ANTLR initialization logic.
 */
public class GDLHandler {
  /**
   * GDL listener implementation.
   */
  private GDLLoader loader;

  /**
   * Private constructor to avoid external initialization.
   *
   * @param loader GDL loader
   */
  private GDLHandler(GDLLoader loader) {
    this.loader = loader;
  }

  /**
   * Append the given GDL string to the current database.
   *
   * @param asciiString GDL string (must not be {@code null}).
   */
  public void append(String asciiString) {
    if (asciiString == null) {
      throw new IllegalArgumentException("AsciiString must not be null");
    }
    ANTLRInputStream antlrInputStream = new ANTLRInputStream(asciiString);
    GDLLexer lexer = new GDLLexer(antlrInputStream);
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));
    // update the loader state while walking the parse tree
    new ParseTreeWalker().walk(loader, parser.database());
  }

  /**
   * Returns a collection of all graphs defined in the GDL script.
   *
   * @return graph collection
   */
  public Collection<Graph> getGraphs() {
    return loader.getGraphs();
  }

  /**
   * Returns a collection of all vertices defined in the GDL script.
   *
   * @return vertex collection
   */
  public Collection<Vertex> getVertices() {
    return loader.getVertices();
  }

  /**
   * Returns a collection of all edges defined in the GDL script.
   *
   * @return edge collection
   */
  public Collection<Edge> getEdges() {
    return loader.getEdges();
  }

  /**
   * Returns the predicates defined by the query in CNF.
   *
   * @return predicates
   */
  public Optional<Predicate> getPredicates() { return loader.getPredicates(); }

  /**
   * Returns the graph cache that contains a mapping from variables used in the GDL script to
   * graph instances.
   *
   * @return immutable graph cache
   */
  public Map<String, Graph> getGraphCache() {
    return loader.getGraphCache();
  }

  /**
   * Returns the vertex cache that contains a mapping from variables used in the GDL script to
   * vertex instances.
   *
   * @return immutable vertex cache
   */
  public Map<String, Vertex> getVertexCache() {
    return loader.getVertexCache();
  }

  /**
   * Returns the edge cache that contains a mapping from variables used in the GDL script to edge
   * instances.
   *
   * @return immutable edge cache
   */
  public Map<String, Edge> getEdgeCache() {
    return loader.getEdgeCache();
  }

  /**
   * Builds a GDL Handler.
   */
  public static class Builder {

    /**
     * Graph label.
     */
    private String graphLabel = "__GRAPH";

    /**
     * Vertex label.
     */
    private String vertexLabel = "__VERTEX";

    /**
     * Edge label.
     */
    private String edgeLabel = "__EDGE";

    /**
     * Default graph label is used if none is set in the GDL script.
     *
     * @param graphLabel graph label (must not be {@code null}).
     * @return builder
     */
    public Builder setDefaultGraphLabel(String graphLabel) {
      this.graphLabel = graphLabel;
      return this;
    }

    /**
     * Default vertex label is used if none is set in the GDL script.
     *
     * @param vertexLabel vertex label (must not be {@code null}).
     * @return builder
     */
    public Builder setDefaultVertexLabel(String vertexLabel) {
      this.vertexLabel = vertexLabel;
      return this;
    }

    /**
     * Default edge label is used if none is set in the GDL script.
     *
     * @param edgeLabel edge label (must not be {@code null}).
     * @return builder
     */
    public Builder setDefaultEdgeLabel(String edgeLabel) {
      this.edgeLabel = edgeLabel;
      return this;
    }

    /**
     * Initialize GDL Handler from given ASCII String.
     *
     * @param asciiString GDL string (must not be {@code null}).
     * @return GDL handler
     */
    public GDLHandler buildFromString(String asciiString) {
      ANTLRInputStream antlrInputStream = new ANTLRInputStream(asciiString);
      return build(antlrInputStream);
    }

    /**
     * Initializes GDL Handler from given input stream.
     *
     * @param stream InputStream (must not be {@code null}).
     * @return GDL handler
     * @throws IOException
     */
    public GDLHandler buildFromStream(InputStream stream) throws IOException {
      ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
      return build(antlrInputStream);
    }

    /**
     * Initializes GDL Handler from given file.
     *
     * @param fileName GDL file (must not be {@code null}).
     * @return GDL handler
     */
    public GDLHandler buildFromFile(String fileName) throws IOException {
      ANTLRInputStream antlrInputStream = new ANTLRFileStream(fileName);
      return build(antlrInputStream);
    }

    /**
     * Checks valid input and creates GDL Handler.
     *
     * @param antlrInputStream ANTLR input stream
     * @return GDL handler
     */
    private GDLHandler build(ANTLRInputStream antlrInputStream) {
      if (graphLabel == null) {
        throw new IllegalArgumentException("Graph label must not be null.");
      }
      if (vertexLabel == null) {
        throw new IllegalArgumentException("Vertex label must not be null.");
      }
      if (edgeLabel == null) {
        throw new IllegalArgumentException("Edge label must not be null.");
      }

      GDLLexer lexer = new GDLLexer(antlrInputStream);
      GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

      GDLLoader loader = new GDLLoader(graphLabel, vertexLabel, edgeLabel);
      new ParseTreeWalker().walk(loader, parser.database());
      return new GDLHandler(loader);
    }
  }
}

