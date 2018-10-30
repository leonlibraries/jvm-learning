package org.leon.annotation;

@CheckSetter
public class ProcessorTest
{
  @CheckSetter
  int a;

  static int b;

  ProcessorTest(){}

  void setA(int a)
  {
    this.a = a;
  }
}
