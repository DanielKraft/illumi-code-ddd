package illumi.code.ddd.model.artifacts;

import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDIssueType;

import java.util.List;

import org.neo4j.driver.v1.Record;

public class Method {

  private String visibility;
  private String name;
  private String signature;

  /**
   * Constructor of method using Neo4j record.
   *
   * @param record : Neo4j result
   */
  public Method(Record record) {
    this.visibility = record.get("visibility").asString();
    this.name = record.get("name").asString();
    this.signature = record.get("signature").asString();
  }

  /**
   * Copy constructor.
   *
   * @param method : other method
   */
  public Method(Method method) {
    this.visibility = method.visibility;
    this.name = method.name;
    this.signature = method.signature;
  }

  /**
   * Constructor of method.
   *
   * @param visibility : visibility of the method
   * @param name       : name of the method
   * @param signature  : signature of the method
   */
  public Method(String visibility, String name, String signature) {
    this.visibility = visibility;
    this.name = name;
    this.signature = signature;
  }

  public String getVisibility() {
    return visibility;
  }

  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }

  public String getName() {
    return name;
  }

  public String getLowerName() {
    return name.toLowerCase();
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * Parse method to UML signature.
   *
   * @return UML signature as String
   */
  @SuppressWarnings("CheckStyle")
  public String getUMLSignature() {
    String umlVisibility = File.getUMLVisibility(visibility);

    String[] split = signature.split(" ");
    return String.format("%s %s: %s", umlVisibility, split[1], split[0]);
  }

  /**
   * Evaluates needed methods of artifact.
   *
   * @param artifact : artifact
   * @param fitness  : fitness of the artifact
   */
  public static void evaluateNeededMethods(Class artifact, DDDFitness fitness) {
    int ctr = 0;
    for (Method method : artifact.getMethods()) {
      if (isNeededMethod(method)) {
        ctr++;
      }
    }
    if (ctr >= 2) {
      fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
    } else if (artifact.getSuperClass() == null) {
      fitness.addFailedCriteria(DDDIssueType.MAJOR,
          String.format(
              "The Entity '%s' does not contains all needed methods (equals/hashCode).",
              artifact.getName()));
    }
  }

  private static boolean isNeededMethod(Method method) {
    // equals() or hashCode()?
    return method.getName().equalsIgnoreCase("equals")
        || method.getName().equalsIgnoreCase("hashCode");
  }

  static void evaluateEntity(Class artifact, Field field, DDDFitness fitness) {
    boolean containsSetter = false;
    boolean containsGetter = false;
    for (Method method : artifact.getMethods()) {
      if (method.getName().equalsIgnoreCase("set" + field.getName())) {
        containsSetter = true;

      } else if (method.getName().equalsIgnoreCase("get" + field.getName())) {
        containsGetter = true;
      }
    }
    fitness.addIssue(containsGetter, DDDIssueType.MINOR,
        String.format(
            "The field '%s' of the Entity '%s' has no Getter.",
            field.getName(), artifact.getName()));


    fitness.addIssue(containsSetter, DDDIssueType.MINOR,
        String.format(
            "The field '%s' of the Entity '%s' has no setter.",
            field.getName(), artifact.getName()));
  }

  static void evaluateValueObject(Class artifact, Field field, DDDFitness fitness) {
    boolean containsSetter = false;
    boolean containsGetter = false;
    for (Method method : artifact.getMethods()) {
      if (method.getName().equalsIgnoreCase("set" + field.getName())) {
        containsSetter = true;

        fitness.addIssue(isMethodImmutable(artifact, method), DDDIssueType.CRITICAL,
            String.format("The method '%s(...)' is not immutable.", method.getName()));

      } else if (method.getName().equalsIgnoreCase("get" + field.getName())) {
        containsGetter = true;
        fitness.addFailedCriteria(DDDIssueType.INFO,
            String.format(
                "The getter '%s()' should be named '%s'.",
                method.getName(), field.getName()));
      } else if (method.getName().equalsIgnoreCase(field.getName())) {
        containsGetter = true;
        fitness.addSuccessfulCriteria(DDDIssueType.INFO);
      }
    }
    fitness.addIssue(containsGetter, DDDIssueType.MINOR,
        String.format(
            "The field '%s' of the Value Object '%s' has no Getter.",
            field.getName(), artifact.getName()));


    fitness.addIssue(containsSetter, DDDIssueType.MINOR,
        String.format(
            "The field '%s' of the Value Object '%s' has no setter.",
            field.getName(), artifact.getName()));
  }

  private static boolean isMethodImmutable(Class artifact, Method method) {
    return method.getVisibility().contains("private")
        || method.getSignature().split(" ")[0].contains(artifact.getPath());
  }

  static void evaluateDomainEvent(Class artifact, Field field, DDDFitness fitness) {
    boolean containsGetter = false;

    for (Method method : artifact.getMethods()) {
      if (method.getName().toUpperCase().startsWith("GET" + field.getName().toUpperCase())) {
        containsGetter = true;
      } else if (method.getName().toUpperCase().startsWith("SET" + field.getName().toUpperCase())) {
        fitness.addIssue(isMethodImmutable(artifact, method), DDDIssueType.CRITICAL,
            String.format("The method '%s(...)' is not immutable.", method.getName()));
      }
    }

    fitness.addIssue(containsGetter, DDDIssueType.MINOR,
        String.format(
            "The field '%s' of the Domain Event '%s' has no getter.",
            field.getName(), artifact.getName()));
  }

  /**
   * Evaluates methods of a repository.
   *
   * @param name    : name of the repository
   * @param methods : methods of the repository
   * @param fitness : fitness of the repository
   */
  public static void evaluateRepository(String name, List<Method> methods, DDDFitness fitness) {
    boolean containsNextIdentity = false;
    boolean containsFind = false;
    boolean containsSave = false;
    boolean containsDelete = false;
    boolean containsContains = false;
    boolean containsUpdate = false;

    for (Method method : methods) {
      if (isNextIdentity(method)) {
        containsNextIdentity = true;
      } else if (isFind(method)) {
        containsFind = true;
      } else if (isSave(method)) {
        containsSave = true;
      } else if (isDelete(method)) {
        containsDelete = true;
      } else if (isContains(method)) {
        containsContains = true;
      } else if (isUpdate(method)) {
        containsUpdate = true;
      }
    }

    fitness.addIssue(containsNextIdentity, DDDIssueType.MAJOR,
        String.format("The Repository '%s' has no nextIdentity method.", name));
    fitness.addIssue(containsFind, DDDIssueType.MAJOR,
        String.format("The Repository '%s' has no findBy/get method.", name));
    fitness.addIssue(containsSave, DDDIssueType.MAJOR,
        String.format("The Repository '%s' has no save/add/insert/put method.", name));
    fitness.addIssue(containsDelete, DDDIssueType.MAJOR,
        String.format("The Repository '%s' has no delete/remove method.", name));
    fitness.addIssue(containsContains, DDDIssueType.MINOR,
        String.format("The Repository '%s' has no contains/exists method.", name));
    fitness.addIssue(containsUpdate, DDDIssueType.MINOR,
        String.format("The Repository '%s' has no update method.", name));
  }

  private static boolean isNextIdentity(Method method) {
    return method.getName().startsWith("nextIdentity");
  }

  private static boolean isFind(Method method) {
    return method.getName().startsWith("findBy")
        || method.getName().startsWith("get");
  }

  private static boolean isSave(Method method) {
    return method.getName().startsWith("save")
        || method.getName().startsWith("add")
        || method.getName().startsWith("insert")
        || method.getName().startsWith("put");
  }

  private static boolean isDelete(Method method) {
    return method.getName().startsWith("delete")
        || method.getName().startsWith("remove");
  }

  private static boolean isContains(Method method) {
    return method.getName().startsWith("contains")
        || method.getName().startsWith("exists");
  }

  private static boolean isUpdate(Method method) {
    return method.getName().startsWith("update");
  }

  /**
   * Evaluates methods of a factory.
   *
   * @param name    : name of the factory
   * @param methods : methods of the factory
   * @param fitness : fitness of the factory
   */
  public static void evaluateFactory(String name, List<Method> methods, DDDFitness fitness) {
    boolean containsCreate = false;
    for (Method method : methods) {
      if (isCreate(method)) {
        containsCreate = true;
        break;
      }
    }

    fitness.addIssue(containsCreate, DDDIssueType.MAJOR,
        String.format("The factory '%s' does not contains a create method.", name));
  }

  private static boolean isCreate(Method method) {
    return method.getName().startsWith("create");
  }
}
