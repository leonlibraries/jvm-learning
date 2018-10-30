package org.leon.annotation;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("org.leon.annotation.CheckSetter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CheckGetterProcessor extends AbstractProcessor
{

  @Override
  public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
  {
    System.out.println(annotations.toString());
    // TODO: annotated ElementKind.FIELD
    // 通过 roundEnv.getElementsAnnotatedWith(CheckSetter.class)) 获取CheckSetter 注解的类或者字段
    for (TypeElement annotatedClass : ElementFilter
        .typesIn(roundEnv.getElementsAnnotatedWith(CheckSetter.class))) {
      for (VariableElement field : ElementFilter.fieldsIn(annotatedClass.getEnclosedElements())) {
        if (!containsGetter(annotatedClass, field.getSimpleName().toString())) {
          processingEnv.getMessager().printMessage(Kind.ERROR, String
              .format("getter not found for '%s.%s'.", annotatedClass.getSimpleName(),
                  field.getSimpleName()));
        }
      }
    }
    return true;
  }


  private static boolean containsGetter(TypeElement element, String name)
  {
    return false;
  }
}
