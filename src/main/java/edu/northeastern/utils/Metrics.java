package edu.northeastern.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * This class contains methods that calculate commonly required Metrics
 * for different detectors.
 */
public class Metrics {

  /**
   * Calculate the Weighted Methods per Class metric
   * WMC is the total complexity of all the methods in a class
   * calculated by summing up the cyclomatic complexities of every
   * method in the class.
   * Data Classes have low WMC since they do not do much more than
   * setting and getting fields.
   *
   * @param type The class whose WMC needs to be calculated
   * @return the WMC metric value
   */
  public static int calculateWMC(CtType<?> type) {
    int totalComplexity = 0;
    for (CtMethod<?> method : type.getMethods()) {
      if (method.getBody() != null) {
        totalComplexity += calculateCyclomaticComplexity(method);
      }
    }
    return totalComplexity;
  }

  /**
   * Calculate the cyclomatic complexity of a method.
   * Cyclomatic complexity is the number of independent paths present
   * in a piece of code
   *
   * @param method The method whose cyclomatic complexity needs to be calculated
   * @return the cyclomatic complexity
   */
  public static int calculateCyclomaticComplexity(CtMethod<?> method) {
      if (method.getBody() == null) {
          return 1;
      }

    int complexity = 1;

    List<Class<? extends CtElement>> decisionNodes = List.of(
        CtIf.class, CtFor.class, CtForEach.class, CtWhile.class,
        CtDo.class, CtConditional.class, CtCatch.class, CtThrow.class
    );

    for (Class<? extends CtElement> node : decisionNodes) {
      complexity += method.getElements(new TypeFilter<>(node)).size();
    }

    for (CtCase<?> switchCase : method.getElements(new TypeFilter<>(CtCase.class))) {
      if (switchCase.getCaseExpression() != null) {
        complexity++;
      }
    }

    for (CtBinaryOperator<?> op : method.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
      if (op.getKind() == BinaryOperatorKind.AND || op.getKind() == BinaryOperatorKind.OR) {

        CtElement parent = op.getParent();
        while (parent != null && !(parent instanceof CtMethod)) {
          if (parent instanceof CtIf || parent instanceof CtLoop ||
              parent instanceof CtConditional) {
            complexity++;
            break;
          }
          parent = parent.getParent();
        }
      }
    }

    return complexity;
  }

  /**
   * Checks if a method is a true accessor (getter/setter) based on both its
   * structural signature and its internal behavior size.
   *
   * @param method        The method to check.
   * @param requirePublic If true, strictly limits to public accessors.
   * @return boolean True if it is a simple getter or setter.
   */
  public static boolean isAccessor(CtMethod<?> method, boolean requirePublic) {
    if (method.getDeclaringType() instanceof CtRecord) {
      return true;
    }
      if (method.isStatic()) {
          return false;
      }
      if (requirePublic && !method.isPublic()) {
          return false;
      }

    String name = method.getSimpleName();
    int paramCount = method.getParameters().size();

      if (method.getType() == null) {
          return false;
      }
    String returnType = method.getType().getSimpleName();

    boolean isGetter = (name.startsWith("get") || name.startsWith("is"))
        && paramCount == 0
        && !returnType.equals("void");

    boolean isSetter = name.startsWith("set")
        && paramCount == 1
        && returnType.equals("void");

    if (!isGetter && !isSetter) {
      return false;
    }

    if (method.getBody() == null) {
      return true;
    }

    return method.getBody().getStatements().size() <= 2;
  }

  /**
   * Calculate the number of Logical Lines of Code
   * A Logical Line of Code is code that is a statement in Java
   *
   * @param executable The class body to check
   * @return the number of LLOC
   */
  public static int calculateLLOC(CtExecutable<?> executable) {
    if (executable.getBody() == null) {
      return 0;
    }
    List<CtStatement> statements =
        executable.getBody().getElements(new TypeFilter<>(CtStatement.class));

    int lloc = 0;
    for (CtStatement stmt : statements) {
      if (!(stmt instanceof CtBlock) && !stmt.isImplicit()) {
        lloc++;
      }
    }

    return lloc;
  }

  public static int calculateLLOC(CtType<?> type) {
    int totalLLOC = 0;

    List<CtExecutable<?>> executables = type.getElements(new TypeFilter<>(CtExecutable.class));

    for (CtExecutable<?> executable : executables) {
      totalLLOC += calculateLLOC(executable);
    }

    return totalLLOC;
  }

  /**
   * Creates a mapped registry of which methods access which internal class fields,
   * while safely ignoring ubiquitous "glue" fields (like loggers) that artificially
   * inflate cohesion metrics. If "glue" fields are used by all methods,
   * then they are not ignored.
   *
   * @param type              The class being analyzed.
   * @param methodsToAnalyze  The filtered list of methods to map.
   * @param ubiquityThreshold The percentage (0.0 to 1.0) at which a field is considered "glue".
   * @return A map of methods to their accessed internal field names.
   */
  public static Map<CtMethod<?>, Set<String>> getMethodFieldUsageMap(
      CtType<?> type,
      List<CtMethod<?>> methodsToAnalyze,
      double ubiquityThreshold) {

    Map<CtMethod<?>, Set<String>> methodFieldUsage = new HashMap<>();
    Map<String, Integer> fieldUsageCounts = new HashMap<>();

    for (CtMethod<?> m : methodsToAnalyze) {
      Set<String> accessedFields = new HashSet<>();
      List<CtFieldAccess<?>> accesses = m.getElements(new TypeFilter<>(CtFieldAccess.class));

      for (CtFieldAccess<?> access : accesses) {
        if (access.getTarget() == null || access.getTarget().toString().equals("this") ||
            access.getTarget().getType() == null) {
          accessedFields.add(access.getVariable().getSimpleName());
        } else {
          String targetType = access.getTarget().getType().getSimpleName();
          if (targetType.equals(type.getSimpleName())) {
            accessedFields.add(access.getVariable().getSimpleName());
          }
        }
      }
      methodFieldUsage.put(m, accessedFields);

      for (String f : accessedFields) {
        fieldUsageCounts.put(f, fieldUsageCounts.getOrDefault(f, 0) + 1);
      }
    }

    int totalMethods = methodsToAnalyze.size();
    Set<String> glueFields = new HashSet<>();
    for (Map.Entry<String, Integer> entry : fieldUsageCounts.entrySet()) {
      if (((double) entry.getValue() / totalMethods) >= ubiquityThreshold) {
        glueFields.add(entry.getKey());
      }
    }

    boolean removingGlueWouldEraseAllUsage = methodFieldUsage.values().stream()
        .allMatch(fields -> {
          Set<String> remaining = new HashSet<>(fields);
          remaining.removeAll(glueFields);
          return remaining.isEmpty();
        });

    if (!removingGlueWouldEraseAllUsage) {
      for (Set<String> fields : methodFieldUsage.values()) {
        fields.removeAll(glueFields);
      }
    }

    return methodFieldUsage;
  }

}
