package org.s1ck.gdl;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class GDLHandlerTest {

  @Test
  public void initFromStringTest() {
    GDLHandler handler = GDLHandler.initFromString("[()]");
    Assert.assertEquals("wrong number of graphs", 1, handler.getVertices().size());
    Assert.assertEquals("wrong number of vertices", 1, handler.getVertices().size());
  }

  @Test
  public void initFromStreamTest() throws IOException {
    InputStream inputStream = GDLHandler.class.getResourceAsStream("/single_graph.gdl");
    GDLHandler handler = GDLHandler.initFromStream(inputStream);
    Assert.assertEquals("wrong number of graphs", 1, handler.getVertices().size());
    Assert.assertEquals("wrong number of vertices", 1, handler.getVertices().size());
  }

  @Test
  public void initFromFileTest() throws IOException {
    String fileName = GDLHandler.class.getResource("/single_graph.gdl").getFile();
    GDLHandler handler = GDLHandler.initFromFile(fileName);
    Assert.assertEquals("wrong number of graphs", 1, handler.getVertices().size());
    Assert.assertEquals("wrong number of vertices", 1, handler.getVertices().size());
  }
}
