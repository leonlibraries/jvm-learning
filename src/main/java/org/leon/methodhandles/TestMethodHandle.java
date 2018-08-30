package org.leon.methodhandles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class TestMethodHandle
{

  private int factor = 100;


  public static void main(String[] args) throws Throwable
  {
    /*
      create a Lookup （method handle 的工厂)
     */
    // provides access to public methods
    MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    // provides access to all methods
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    /*
      create a MethodType (代表期望的参数类型以及返回值类型)

      方法签名包括：方法名 和 参数，不包括返回值，是Java语义上的，对于重载有意义
     */
    MethodType methodType = MethodType.methodType(String.class, Father.class);
    MethodHandle methodHandle = lookup.findVirtual(Father.class, "echo", methodType);

    // 执行 invoke 自动匹配，参数可以 Father 子类实例 (模拟 多态)
    String returnValue1 = (String) methodHandle.invoke(new Child1(), new Father());
    String returnValue2 = (String) methodHandle.invoke(new Child2(), new Father());
    // 执行 invokeExact 精确匹配 new Father 只能是Father实例，不能是其子类实例
    String returnValue3 = (String) methodHandle.invokeExact(new Father(), new Father());

    // 多态结果
    System.out.println("The return value is " + returnValue1);
    System.out.println("The return value is " + returnValue2);
    System.out.println("The return value is " + returnValue3);
    /*
      方法句柄增删改参数操作
     */
    // invoke 会调用 asType 方法，生成一个适配句柄， 对传入参数进行适配

  }

  public int getFactor()
  {
    return factor;
  }

  public void setFactor(int factor)
  {
    this.factor = factor;
  }
}

class Child1 extends Father
{

  @Override
  public void print()
  {
    System.out.println("Im Child1");
  }

  @Override
  public String echo(Father obj)
  {
    System.out.println("====" + obj + "====");
    obj.print();
    return "Child1 :: invoke";
  }
}

class Child2 extends Father
{

  @Override
  public void print()
  {
    System.out.println("Im Child2");
  }

  @Override
  public String echo(Father obj)
  {
    System.out.println("====" + obj + "====");
    obj.print();
    return "Child2 :: invoke";
  }
}

class Father
{

  public void print()
  {
    System.out.println("Im Father");
  }

  public String echo(Father obj)
  {
    System.out.println("====" + obj + "====");
    obj.print();
    return "Father :: invoke";
  }
}
