package me.karjan.enumvisitorgenerator;

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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import static me.karjan.enumvisitorgenerator.PluginUtil.BLANK_SPACE;
import static me.karjan.enumvisitorgenerator.PluginUtil.DEFAULT_SOURCE_DIRECTORY;
import static me.karjan.enumvisitorgenerator.PluginUtil.JAVA_FILE_EXT;
import static me.karjan.enumvisitorgenerator.PluginUtil.JAVA_IDENTIFIER_REGEX;
import static me.karjan.enumvisitorgenerator.PluginUtil.LINE_SEPARATOR;
import static me.karjan.enumvisitorgenerator.PluginUtil.PACKAGE_SEPARATOR;
import static me.karjan.enumvisitorgenerator.PluginUtil.PATH_SEPARATOR;
import static me.karjan.enumvisitorgenerator.PluginUtil.VISITOR_FILE_EXT;

/**
 * TODO: javadoc
 */
public class VisitorGeneratorTask extends DefaultTask {

  @InputDirectory
  @Optional
  File source = getProject().file(DEFAULT_SOURCE_DIRECTORY);

  @OutputDirectory
  @Optional
  File destination = getProject().getLayout().getBuildDirectory().file("generated-src/visgen").get().getAsFile();

  /**
   * TODO: javadoc
   */
  @TaskAction
  void generatorVisitorClass() throws IOException {
    ConfigurableFileTree fileTree = getProject().fileTree(source);
    fileTree.include("**/*" + VISITOR_FILE_EXT);
    fileTree.forEach(file -> {
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
    });
  }

  private String[] determineEnumLocationSegments(File file) {
    String sourcePathStr = source.getPath();
    String filePathStr = file.getPath();
    return filePathStr.replace(sourcePathStr, "")
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
      String[] enumLocationSegments, String enumName, List<String> enumMembers)
      throws IOException {
    String enumPackage = String.join(PACKAGE_SEPARATOR, enumLocationSegments);
    String enumLocation = String.join(PATH_SEPARATOR, enumLocationSegments);

    if (PluginUtil.isEmpty(enumName)) {
      throwTaskExecutionException("The enum name is empty");
    }
    checkForValidJavaIdentifier(enumName);

    if (PluginUtil.isEmpty(enumPackage)) {
      throwTaskExecutionException("The location of this enum maps to the default Java package: " + enumName);
    }

    if (enumMembers.size() == 0) {
      throwTaskExecutionException("The enum definition " + enumName + " does not have any members");
    }

    for (String member : enumMembers) {
      checkForValidJavaIdentifier(member);
    }

    Path fullDestination = FileSystems.getDefault()
        .getPath(destination.toString(), enumLocation);
    fullDestination.toFile().mkdirs();
    File enumVisitorFile = new File(fullDestination.toString(),  enumName + JAVA_FILE_EXT);

    enumVisitorFile.createNewFile();

    StringBuilder fileContents = new StringBuilder();

    fileContents.append("package " + enumPackage + ";")
        .append(LINE_SEPARATOR)
        .append(LINE_SEPARATOR);

    fileContents.append("enum " + enumName + " {")
        .append(LINE_SEPARATOR);

    StringJoiner memberDefs = new StringJoiner(
      "," + LINE_SEPARATOR + LINE_SEPARATOR,
      "",
      ";" + LINE_SEPARATOR + LINE_SEPARATOR
    );
    enumMembers.forEach(member -> {
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

    fileContents
        .append("  public interface " + enumName + "Visitor<E> {")
        .append(LINE_SEPARATOR);

    enumMembers.forEach(member -> {
      fileContents
          .append("    E visit" + member + "();")
          .append(LINE_SEPARATOR);
    });

    fileContents
        .append("  }")
        .append(LINE_SEPARATOR);

    fileContents
        .append("}")
        .append(LINE_SEPARATOR);

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

  /**
   * TODO: javadoc
   */
  File getSource() {
    return source;
  }

  /**
   * TODO: javadoc
   */
  File getDestination() {
    return destination;
  }
    
}
