package org.leon.methodhandles.jol;

import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

public class JolUtils
{

  @Test
  public void displayAB()
  {
    System.out.println(VM.current().details());
    System.out.println(ClassLayout.parseClass(A.class).toPrintable());
    System.out.println(ClassLayout.parseClass(B.class).toPrintable());
  }
}

class A
{

  long l;
  int i;
}

class B extends A
{

  long l;
  int i;
}
