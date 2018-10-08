package org.leon.jit;

/**
 * 在 new Sub 之前，整个Classloader只加载了Add（唯一BinaryOp实现类），基于类层次分析完全去虚化，实现方法内联，Sub加载进来后，类层次分析无用，去虚化假设失败，内联后的机器码被置为不允许访问（失效激进优化）
 */
// -XX:CompileCommand="dontinline org.leon.jit.DeoptimizedForCL.test" -XX:+PrintCompilation
public class DeoptimizedForCL
{

  static abstract class BinaryOp
  {

    public abstract int apply(int a, int b);
  }

  static class Add extends BinaryOp
  {

    public int apply(int a, int b)
    {
      return a + b;
    }
  }

  static class Sub extends BinaryOp
  {

    public int apply(int a, int b)
    {
      return a - b;
    }
  }

  public static int test(BinaryOp op)
  {
    return op.apply(2, 1);
  }

  public static void main(String[] args) throws Exception
  {
    Add add = new Add();
    for (int i = 0; i < 400_000; i++) {
      test(add);
    }

    Thread.sleep(2000);
    System.out.println("Loading Sub");
    Sub[] array = new Sub[0]; // Load class Sub
    // Expect output: "org.leon.jit.DeoptimizedForCL::test (7 bytes)   made not entrant"
    // 意思是内联后的test方法，不允许之后的caller访问
    Thread.sleep(2000);
  }
}
