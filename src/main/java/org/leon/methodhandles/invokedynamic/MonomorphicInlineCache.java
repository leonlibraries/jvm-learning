package org.leon.methodhandles.invokedynamic;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

/**
 * 为了支持调用任意类的 race 方法，需要实现一个简单的内联缓存
 *
 * @author leonwong
 */
public class MonomorphicInlineCache
{

  private final MethodHandles.Lookup lookup;

  private final String name;


  public MonomorphicInlineCache(Lookup lookup, String name)
  {
    this.lookup = lookup;
    this.name = name;
  }

  private Class<?> cacheClass = null;

  private MethodHandle mh = null;

  public void invoke(Object receiver) throws Throwable
  {
    if (cacheClass != receiver.getClass()) {
      cacheClass = receiver.getClass();
      mh = lookup.findVirtual(cacheClass, name, MethodType.methodType(void.class));
    }
    mh.invoke(receiver);
  }

  public static CallSite bootstrap(MethodHandles.Lookup l, String name, MethodType callSiteType)
      throws Throwable
  {
    MonomorphicInlineCache ic = new MonomorphicInlineCache(l, name);
    MethodHandle mh = l.findVirtual(MonomorphicInlineCache.class, "invoke",
        MethodType.methodType(void.class, Object.class));
    return new ConstantCallSite(mh.bindTo(ic));
  }

}
