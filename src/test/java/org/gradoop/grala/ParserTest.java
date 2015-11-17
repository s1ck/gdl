package org.gradoop.grala;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.gradoop.grala.model.Edge;
import org.gradoop.grala.model.Graph;
import org.gradoop.grala.model.Vertex;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ParserTest {

  @Test
  public void testListener() throws IOException {
    String file = "/social_network.gdl";
    InputStream inputStream = getClass().getResourceAsStream(file);

    GDLLexer lexer = new GDLLexer(new ANTLRInputStream(inputStream));
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

    ParseTreeWalker walker = new ParseTreeWalker();
    GDLLoader loader = new GDLLoader();

    walker.walk(loader, parser.database());

    for (Graph g : loader.getGraphs()) {
      System.out.println(g);
    }

    for (Vertex vertex : loader.getVertices()) {
      System.out.println(vertex);
    }

    for (Edge edge : loader.getEdges()) {
      System.out.println(edge);
    }

    System.out.println(loader.getGraphCache());
    System.out.println(loader.getVertexCache());
    System.out.println(loader.getEdgeCache());
  }
}
