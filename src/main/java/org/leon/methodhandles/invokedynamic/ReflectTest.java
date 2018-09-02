package org.leon.methodhandles.invokedynamic;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Depends on org.ow2.asm/asm-commons/4.0
 *
 * Performance is highly dependent on method structure. Introducing a polymorphic call site
 * drastically reduces performance as the previously optimized method is deoptimized.
 */
public class ReflectTest implements Opcodes
{

  private static final int ITER = Integer.MAX_VALUE;

  public static class TestClassLoader extends ClassLoader
  {

    public Class<?> defineClass(final String name, final byte[] b)
    {
      return defineClass(name, b, 0, b.length);
    }
  }

  int i = 0;

  public void print()
  {
    i++;
  }

  public static void main(String[] args) throws Exception
  {
    Constructor<?> constructor = createClass();
    final ReflectTest reflectTest = new ReflectTest();
    final Runnable indyRunnable = (Runnable) constructor.newInstance(reflectTest);
    indyRunnable.run();
    Runnable runnable = new Runnable()
    {
      @Override
      public void run()
      {
        reflectTest.print();
      }
    };

    System.out.println("First set");
    testDirect(reflectTest);
    testInnerClass(runnable);
    testInvokeDynamic(indyRunnable);
    testSharedInnerClass(runnable);
    testSharedRunnable(indyRunnable);
    System.out.println("Second set");
    testDirect(reflectTest);
    testInnerClass(runnable);
    testInvokeDynamic(indyRunnable);
    testSharedInnerClass(runnable);
    testSharedRunnable(indyRunnable);
  }

  private static void testInvokeDynamic(Runnable indyRunnable)
  {
    long start = System.currentTimeMillis();
    for (int i = 0; i < ITER; i++) {
      indyRunnable.run();
    }
    System.out.println(" invokeDynamic/ms: " + (System.currentTimeMillis() - start));
  }

  private static void testInnerClass(Runnable runnable)
  {
    long start = System.currentTimeMillis();
    for (int i1 = 0; i1 < ITER; i1++) {
      runnable.run();
    }
    System.out.println(" testInnerClass/ms: " + (System.currentTimeMillis() - start));
  }

  private static void testSharedRunnable(Runnable runnable)
  {
    long start = System.currentTimeMillis();
    for (int i = 0; i < ITER; i++) {
      runnable.run();
    }
    System.out.println(" sharedInvokeDynamic/ms " + runnable.getClass().getName() + ": " + (
        System.currentTimeMillis() - start));
  }

  private static void testSharedInnerClass(Runnable runnable)
  {
    testSharedRunnable(runnable);
  }

  private static void testDirect(ReflectTest reflectTest)
  {
    long start = System.currentTimeMillis();
    for (int i = 0; i < ITER; i++) {
      reflectTest.print();
    }
    System.out.println(" direct/ms: " + (System.currentTimeMillis() - start));
  }

  private static Constructor<?> createClass() throws Exception
  {
    TestClassLoader LOADER = new TestClassLoader();
    Class<?> aClass = LOADER
        .defineClass("org.leon.methodhandles.invokedynamic.IndyRunnable", createBytes());
    return aClass.getConstructor(ReflectTest.class);
  }

  private static final Handle BOOTSTRAP_METHOD =
      new Handle(H_INVOKESTATIC, "org/leon/methodhandles/invokedynamic/ReflectTest", "bootstrap",
          MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
              MethodType.class).toMethodDescriptorString());

  public static byte[] createBytes() throws Exception
  {

    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    FieldVisitor fv;
    MethodVisitor mv;

    cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, "org/leon/methodhandles/invokedynamic/IndyRunnable",
        null, "java/lang/Object",
        new String[]{"java/lang/Runnable"});

    cw.visitSource("IndyRunnable.java", null);

    {
      fv = cw
          .visitField(ACC_PRIVATE, "rt", "Lorg/leon/methodhandles/invokedynamic/ReflectTest;", null,
              null);
      fv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>",
          "(Lorg/leon/methodhandles/invokedynamic/ReflectTest;)V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(7, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(8, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitFieldInsn(PUTFIELD, "org/leon/methodhandles/invokedynamic/IndyRunnable", "rt",
          "Lorg/leon/methodhandles/invokedynamic/ReflectTest;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(9, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lorg/leon/methodhandles/invokedynamic/IndyRunnable;", null, l0,
          l3, 0);
      mv.visitLocalVariable("rt", "Lorg/leon/methodhandles/invokedynamic/ReflectTest;", null, l0,
          l3, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
    }
    {
      GeneratorAdapter ga = new GeneratorAdapter(ACC_PUBLIC, Method.getMethod("void run ()"), null,
          null, cw);
      ga.loadThis();
      ga.getField(Type.getType("org/leon/methodhandles/invokedynamic/IndyRunnable"), "rt",
          Type.getType(ReflectTest.class));
      ga.invokeDynamic("print", "(Lorg/leon/methodhandles/invokedynamic/ReflectTest;)V",
          BOOTSTRAP_METHOD);
      ga.returnValue();
      ga.endMethod();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type)
      throws NoSuchMethodException, IllegalAccessException
  {
    MethodHandle mine = MethodHandles.lookup()
        .findVirtual(ReflectTest.class, "print", MethodType.methodType(Void.TYPE));
    return new ConstantCallSite(mine);
  }
}
