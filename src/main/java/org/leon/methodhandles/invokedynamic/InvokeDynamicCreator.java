package org.leon.methodhandles.invokedynamic;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

public class InvokeDynamicCreator
{

  public static void main(final String[] args) throws Exception
  {
    final String outputClassName = "org/leon/methodhandles/invokedynamic/Dynamic";
    try (FileOutputStream fos = new FileOutputStream(
        new File("target/classes/" + outputClassName + ".class")
    )) {
      fos.write(dump(outputClassName, "bootstrap", "()V"));
    }
  }

  /**
   * 生成类字节码
   *
   * @param outputClassName 类文件路径+名称
   * @param bsmName 启动方法名称
   * @param targetMethodDescriptor 目标方法描述符
   */
  public static byte[] dump(
      String outputClassName, String bsmName, String targetMethodDescriptor
  ) throws Exception
  {
    final ClassWriter cw = new ClassWriter(0);
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
    Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC,
        "org/leon/methodhandles/invokedynamic/InvokeDynamicCreator", bsmName,
        mt.toMethodDescriptorString());

    mv.visitInvokeDynamicInsn("runDynamic", targetMethodDescriptor, bootstrap);
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 1);
    mv.visitEnd();

    cw.visitEnd();

    return cw.toByteArray();
  }

  private static void targetMethod()
  {
    System.out.println("Hello World!");
  }

  public static CallSite bootstrap(
      MethodHandles.Lookup caller, String name, MethodType callSiteType)
      throws NoSuchMethodException, IllegalAccessException
  {
    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    // target method 是静态的，因此需要用 lookupClass()
    final Class currentClass = lookup.lookupClass();
    final MethodType targetSignature = MethodType.methodType(void.class);
    final MethodHandle targetMH = lookup.findStatic(currentClass, "targetMethod", targetSignature);
    return new ConstantCallSite(targetMH.asType(callSiteType));
  }
}
