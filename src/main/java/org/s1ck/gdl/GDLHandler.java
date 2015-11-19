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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

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
   * Creates a new GDL Handler from the given GDL string.
   *
   * @param gdlString GDL string
   * @return GDL handler
   */
  public static GDLHandler initFromString(String gdlString) {
    return new GDLHandler(init(new ANTLRInputStream(gdlString)));
  }

  /**
   * Creates a new GDL handler from the given GDL input stream.
   *
   * @param gdlStream GDL input stream
   * @return GDL handler
   * @throws IOException
   */
  public static GDLHandler initFromStream(InputStream gdlStream) throws IOException {
    return new GDLHandler(init(new ANTLRInputStream(gdlStream)));
  }

  /**
   * Creates a new GDL handler from the given GDL file.
   *
   * @param fileName path to a file that contains GDL input
   * @return GDL handler
   * @throws IOException
   */
  public static GDLHandler initFromFile(String fileName) throws IOException {
    return new GDLHandler(init(new ANTLRFileStream(fileName)));
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
   * Initializes the GDL loader from the given ANTLR input stream.
   *
   * @param antlrInputStream ANTLR input stream
   * @return GDL loader
   */
  private static GDLLoader init(ANTLRInputStream antlrInputStream) {
    GDLLexer lexer = new GDLLexer(antlrInputStream);
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

    ParseTreeWalker walker = new ParseTreeWalker();
    GDLLoader loader = new GDLLoader();
    walker.walk(loader, parser.database());
    return loader;
  }
 }
