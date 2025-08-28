package me.karjan.enumvisitor;

import static me.karjan.enumvisitor.EnumVisitorUtil.BLANK_SPACE;
import static me.karjan.enumvisitor.EnumVisitorUtil.DEFAULT_SOURCE_DIRECTORY;
import static me.karjan.enumvisitor.EnumVisitorUtil.JAVA_FILE_EXT;
import static me.karjan.enumvisitor.EnumVisitorUtil.JAVA_IDENTIFIER_REGEX;
import static me.karjan.enumvisitor.EnumVisitorUtil.LINE_SEPARATOR;
import static me.karjan.enumvisitor.EnumVisitorUtil.PACKAGE_SEPARATOR;
import static me.karjan.enumvisitor.EnumVisitorUtil.PATH_SEPARATOR;
import static me.karjan.enumvisitor.EnumVisitorUtil.VISITOR_FILE_EXT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

/**
 * A gradle Task which performs the work to generate the Java source files for an enum including the
 * visitor pattern for it.
 */
class GenerateEnumVisitorsTask extends DefaultTask {

  ProjectLayout projectLayout;

  /** The location of the source definition files. */
  @InputDirectory @Optional File source = getProject().file(DEFAULT_SOURCE_DIRECTORY);

  /** The location of the generated Java files. */
  @OutputDirectory @Optional
  File destination =
      getProject().getLayout().getBuildDirectory().file("generated-src/enumvis").get().getAsFile();

  @Inject
  public GenerateEnumVisitorsTask(ProjectLayout projectLayout) {
    this.projectLayout = projectLayout;
  }

  /** Getter for {@link #source}. */
  File getSource() {
    return source;
  }

  /** Getter for {@link #destination}. */
  File getDestination() {
    return destination;
  }

  /**
   * Generate all enum with visitor pattern Java source code files for definitions in the source
   * directory.
   */
  @TaskAction
  void generatorEnumVisitors() {
    projectLayout
        .getProjectDirectory()
        .dir(DEFAULT_SOURCE_DIRECTORY)
        .getAsFileTree()
        .visit(visitor -> visitFile(visitor));
  }

  private void visitFile(FileVisitDetails visitor) {
    File file = visitor.getFile();
    if (file.getName().endsWith(VISITOR_FILE_EXT)) {
      try {
        String[] enumLocationSegments = determineEnumLocationSegments(file);
        String fileName = file.getName();
        String enumName = fileName.substring(0, fileName.lastIndexOf('.'));
        List<String> enumMembers = readFileLinesToSet(file);
        generateEnumVisitorSourceCode(enumLocationSegments, enumName, enumMembers);
      } catch (IOException e) {
        // ignore
      }
      getLogger().info(file.getName());
    }
  }

  private String[] determineEnumLocationSegments(File file) {
    String sourcePathStr = source.getPath();
    String filePathStr = file.getPath();
    return filePathStr
        .replace(sourcePathStr, "")
        .replace(file.getName(), "")
        .replace(PATH_SEPARATOR, PACKAGE_SEPARATOR)
        .replace(BLANK_SPACE, "")
        .substring(1)
        .split("\\" + PACKAGE_SEPARATOR);
  }

  private List<String> readFileLinesToSet(File file) throws IOException {
    List<String> linesList = new ArrayList<>();
    Set<String> linesSet = new HashSet<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (linesSet.contains(line)) {
          throwTaskExecutionException("Duplicate line in file " + file.getName() + ": " + line);
        }
        linesSet.add(line);
        linesList.add(line);
      }
    }
    return linesList;
  }

  private void generateEnumVisitorSourceCode(
      String[] enumLocationSegments, String enumName, List<String> enumMembers) throws IOException {
    String enumPackage = String.join(PACKAGE_SEPARATOR, enumLocationSegments);
    String enumLocation = String.join(PATH_SEPARATOR, enumLocationSegments);

    if (EnumVisitorUtil.isEmpty(enumName)) {
      throwTaskExecutionException("The enum name is empty");
    }
    checkForValidJavaIdentifier(enumName);

    if (EnumVisitorUtil.isEmpty(enumPackage)) {
      throwTaskExecutionException(
          "The location of this enum maps to the default Java package: " + enumName);
    }

    if (enumMembers.size() == 0) {
      throwTaskExecutionException("The enum definition " + enumName + " does not have any members");
    }

    for (String member : enumMembers) {
      checkForValidJavaIdentifier(member);
    }

    Path fullDestination = FileSystems.getDefault().getPath(destination.toString(), enumLocation);
    fullDestination.toFile().mkdirs();
    File enumVisitorFile = new File(fullDestination.toString(), enumName + JAVA_FILE_EXT);

    enumVisitorFile.createNewFile();

    StringBuilder fileContents = new StringBuilder();

    fileContents
        .append("package " + enumPackage + ";")
        .append(LINE_SEPARATOR)
        .append(LINE_SEPARATOR);

    fileContents.append("public enum " + enumName + " {").append(LINE_SEPARATOR);

    StringJoiner memberDefs =
        new StringJoiner(
            "," + LINE_SEPARATOR + LINE_SEPARATOR, "", ";" + LINE_SEPARATOR + LINE_SEPARATOR);
    enumMembers.forEach(
        member -> {
          StringJoiner memberDef = new StringJoiner(LINE_SEPARATOR);
          memberDef
              .add("  " + member + " {")
              .add("    public <E> E accept(" + enumName + "Visitor<E> visitor) {")
              .add("      return visitor.visit" + member + "();")
              .add("    }")
              .add("  }");
          memberDefs.add(memberDef.toString());
        });
    fileContents.append(memberDefs.toString());

    fileContents
        .append("  public abstract <E> E accept(" + enumName + "Visitor<E> visitor);")
        .append(LINE_SEPARATOR)
        .append(LINE_SEPARATOR);

    fileContents.append("  public interface " + enumName + "Visitor<E> {").append(LINE_SEPARATOR);

    enumMembers.forEach(
        member -> {
          fileContents.append("    E visit" + member + "();").append(LINE_SEPARATOR);
        });

    fileContents.append("  }").append(LINE_SEPARATOR);

    fileContents.append("}").append(LINE_SEPARATOR);

    Files.write(enumVisitorFile.toPath(), fileContents.toString().getBytes());
  }

  private void checkForValidJavaIdentifier(String testString) {
    if (!testString.matches(JAVA_IDENTIFIER_REGEX)) {
      throwTaskExecutionException(testString + " is not a valid identifier for Java.");
    }
  }

  private void throwTaskExecutionException(String causeMsg) {
    throw new TaskExecutionException(this, new Throwable(causeMsg));
  }
}
