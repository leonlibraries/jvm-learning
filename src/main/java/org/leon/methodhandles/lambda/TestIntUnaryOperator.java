package org.leon.methodhandles.lambda;

import java.util.stream.IntStream;
import org.junit.Test;

public class TestIntUnaryOperator
{

  @Test
  public void test1()
  {
    int x = 1;
    IntStream.of(1, 2, 3).map(i -> i * 5).map(i -> i * x);
  }

}
