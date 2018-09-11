package org.leon.methodhandles.gc;


/**
 * 无安全点检测的计数循环带来的长暂停
 */
// -XX:+PrintGC
// -XX:+PrintGCApplicationStoppedTime
// -XX:+PrintSafepointStatistics
// -XX:+UseCountedLoopSafepoints
public class SafepointTest
{

  static double sum = 0;

  public static void foo()
  {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 0x77777777; i++) {
      sum += Math.sqrt(i);
    }

    System.out.println("foo => "+(System.currentTimeMillis() - start)+"ms");
  }

  public static void bar()
  {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 50_000_000; i++) {
      new Object().hashCode();
    }
    System.out.println("bar => "+(System.currentTimeMillis() - start)+"ms");
  }


  public static void main(String[] args)
  {
    new Thread(SafepointTest::foo).start();
    new Thread(SafepointTest::bar).start();
  }
}
