package org.gradoop.grala;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;

public class ParserTest {
  @Test
  public void testExampleField() throws Exception {
    GDLLexer l = new GDLLexer(
      new ANTLRInputStream(getClass().getResourceAsStream("/simple-graph.gdl")));
    GDLParser p = new GDLParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer,
        Object offendingSymbol, int line, int charPositionInLine, String msg,
        RecognitionException e) {
        throw new IllegalStateException(
          "failed to parse at line " + line + " due to " + msg, e);
      }
    });

//    p.graph();

    for (GDLParser.VertexContext v : p.graph().vertex()) {
      System.out.println(v.vertexHeader().VertexLabel());
    }
  }
}
