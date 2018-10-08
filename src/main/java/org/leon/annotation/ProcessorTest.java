package org.leon.annotation;

@CheckSetter
public class ProcessorTest
{
  int a;

  static int b;

  public ProcessorTest setA(int a)
  {
    this.a = a;
    return this;
  }
}
