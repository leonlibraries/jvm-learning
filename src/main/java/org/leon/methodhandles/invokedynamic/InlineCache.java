package org.leon.methodhandles.invokedynamic;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 支持动态调用的内联缓存（简单实现）
 */
public class InlineCache
{

  private final MethodHandles.Lookup lookup;

  private final String name;

  public InlineCache(Lookup lookup, String name)
  {
    this.lookup = lookup;
    this.name = name;
  }

  private Class<?> cachedClass = null;
  private MethodHandle cacheMH = null;


  /**
   * 生成 Dynamic 类字节码
   *
   * @param outputClassName 类文件路径+名称
   * @param bsmName 启动方法名称
   * @param targetMethodDescriptor 目标方法的方法句柄描述符
   */
  public static byte[] dump(
      String outputClassName, String bsmName, String targetMethodDescriptor
  ) throws Exception
  {
    // 如果不设置为 COMPUTE_MAXS 不会自动计算操作栈深度
    final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    MethodVisitor mv;

    // 为引导类搭建基本的元数据
    cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, outputClassName, null, "java/lang/Object", null);

    // 创建标准的 void 构造器 Constructor
    mv = cw.visitMethod(ACC_PUBLIC, "", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "", "()V");
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();

    // 创建标准的 main 方法
    mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    mv.visitCode();
    MethodType mt = MethodType
        .methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);
    // 持有 bootstrap 方法的句柄
    Handle bootstrapMH = new Handle(Opcodes.H_INVOKESTATIC,
        "org/leon/methodhandles/invokedynamic/InlineCache", bsmName,
        mt.toMethodDescriptorString());

    // 调用 Horse 目标 race 方法 （需要传递 Horse 对象实例）
    mv.visitTypeInsn(NEW, "org/leon/methodhandles/invokedynamic/DynamicHorse");
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "org/leon/methodhandles/invokedynamic/DynamicHorse", "<init>",
        "()V");
    mv.visitVarInsn(ASTORE, 0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInvokeDynamicInsn("race", targetMethodDescriptor, bootstrapMH);


    // 调用 Horse 目标 race 方法 （需要传递 Horse 对象实例）
    mv.visitTypeInsn(NEW, "org/leon/methodhandles/invokedynamic/DynamicDeer");
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "org/leon/methodhandles/invokedynamic/DynamicDeer", "<init>",
        "()V");
    mv.visitVarInsn(ASTORE, 0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInvokeDynamicInsn("race", targetMethodDescriptor, bootstrapMH);


    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 1);
    mv.visitEnd();

    cw.visitEnd();

    return cw.toByteArray();
  }


  public static void main(String[] args) throws Exception
  {
    final String outputClassName = "org/leon/methodhandles/invokedynamic/InlineCacheDynamic";
    try (FileOutputStream fos = new FileOutputStream(
        new File("target/classes/" + outputClassName + ".class")
    )) {
      fos.write(dump(outputClassName, "bootstrap", "(Ljava/lang/Object;)V"));
    }
  }

  /**
   * 任何对象，只要有对应的方法（name）即可
   */
  public void invoke(Object obj) throws Throwable
  {
    Class invoker = obj.getClass();
    MethodHandle realMH = lookup.findVirtual(invoker, name, MethodType.methodType(void.class));
    realMH.invoke(obj);
  }

  /**
   * 启动方法
   *
   * @param caller 忽略
   * @param name 目标方法名
   * @param callSiteType 调用点类型
   * @throws NoSuchMethodException 无此方法
   * @throws IllegalAccessException 无权访问
   */
  public static CallSite bootstrap(
      MethodHandles.Lookup caller, String name, MethodType callSiteType
  ) throws NoSuchMethodException, IllegalAccessException
  {
    InlineCache inlineCache = new InlineCache(caller, name);
    MethodHandle mh = caller
        .findVirtual(InlineCache.class, "invoke", MethodType.methodType(void.class, Object.class));
    return new ConstantCallSite(mh.bindTo(inlineCache));
  }
}
