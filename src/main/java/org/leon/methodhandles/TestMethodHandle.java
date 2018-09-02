package org.leon.methodhandles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.junit.Assert;
import org.junit.Test;

public class TestMethodHandle
{

  /**
   * 1.利用方法句柄实现多态调用
   */
  @Test
  public void polymorphicInvoke() throws Throwable
  {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    // 寻找 Father 类中的 echo 方法，并传入相应的方法描述符
    MethodHandle methodHandle = lookup.findVirtual(Father.class, "echo",
        MethodType.methodType(String.class, Father.class));

    Father obj = new Child1();

    String returnValue = (String) methodHandle.invoke(obj, new Child2());

    Assert.assertEquals("Child1 :: invoke", returnValue);
  }

  /**
   * 2.利用 invokeExact 精确匹配参数调用
   */
  @Test
  public void exactlyInvoke() throws Throwable
  {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    // 寻找 Father 类中的 echo 方法，并传入相应的方法描述符
    MethodHandle methodHandle = lookup.findVirtual(Father.class, "echo",
        MethodType.methodType(String.class, Father.class));

    // 这样实例化相当于把 Child1 强转成 Father，因此也是符合 Exact 要求，如果不强转，执行invokeExact会抛错
    Father obj = new Child1();

    String returnValue = (String) methodHandle.invokeExact(obj, new Father());

    Assert.assertEquals("Child1 :: invoke", returnValue);
  }

  /**
   * 3.变更参数类型，以适应 Exact 要求 （模拟 invoke 的自动适配原理）
   */
  @Test
  public void alterMethodType() throws Throwable
  {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    // 寻找 Father 类中的 echo 方法，并传入相应的方法描述符
    MethodHandle methodHandle = lookup.findVirtual(Father.class, "echo",
        MethodType.methodType(String.class, Father.class));

    Child1 obj = new Child1();

    String returnValue = (String) methodHandle
        .asType(MethodType.methodType(String.class, Child1.class, Child2.class))
        .invokeExact(obj, new Child2());

    Assert.assertEquals("Child1 :: invoke", returnValue);
  }

  /**
   * 4.打印调用栈轨迹
   */
  @Test
  public void invokeStackTrace() throws Throwable
  {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    // 寻找 Father 类中的 echo 方法，并传入相应的方法描述符
    MethodHandle methodHandle = lookup.findVirtual(PrintStackTrace.class, "print",
        MethodType.methodType(void.class));
    methodHandle.invokeExact(new PrintStackTrace());
  }

  /**
   * 5.实现动态调用 (failed)
   */
  @Test
  public void invokeDynamic() throws Throwable
  {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle methodHandle = lookup.findVirtual(Father.class, "echo",
        MethodType.methodType(String.class, Father.class));

    //改变调用者身份为 Friend
    Friend f = new Friend();

    String returnValue = (String) methodHandle
        .asType(MethodType.methodType(String.class, Friend.class, Child2.class))
        .invokeExact(f, new Child2());

    Assert.assertEquals("Friend :: invoke", returnValue);
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

/**
 * 毫无血缘关系的动态调用
 */
class Friend
{

  public String echo(Father obj)
  {
    System.out.println("====" + obj + "====");
    obj.print();
    return "Friend :: invoke";
  }
}

/**
 * 通过抛异常的方式打印方法栈
 */
class PrintStackTrace
{

  public void print()
  {
    new Exception().printStackTrace();
  }
}


