package org.gradoop.grala;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class ParserTest {

  @Test
  public void testListener() throws IOException {
    String file = "/database.gdl";
    InputStream inputStream = getClass().getResourceAsStream(file);

    GDLLexer lexer = new GDLLexer(new ANTLRInputStream(inputStream));
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

    ParseTreeWalker walker = new ParseTreeWalker();
    GDLLoader loader = new GDLLoader();

    walker.walk(loader, parser.database());
  }
}
