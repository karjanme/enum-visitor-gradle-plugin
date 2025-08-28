package me.karjan.enumvisitor;

import static me.karjan.enumvisitor.EnumVisitorUtil.JAVA_FILE_EXT;
import static me.karjan.enumvisitor.EnumVisitorUtil.LINE_SEPARATOR;
import static me.karjan.enumvisitor.EnumVisitorUtil.VISITOR_FILE_EXT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Automated functional tests for {@link EnumVisitorPlugin}. */
public class EnumVisitorPluginTest {

  @Rule public TemporaryFolder tempProjectDir = new TemporaryFolder();
  File tempBuildFile;

  @Before
  public void beforeTest() throws IOException {
    tempBuildFile = tempProjectDir.newFile("build.gradle");

    String buildString =
        """
        plugins {
          id 'me.karjan.enumvisitor'
        }
        """;

    Files.write(tempBuildFile.toPath(), buildString.getBytes());
  }

  @Test
  public void testGenerateVisitors_compileJavaDependency() throws IOException {
    tempProjectDir.newFolder("src", "main", "enumvis");
    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(JavaPlugin.COMPILE_JAVA_TASK_NAME)
            .withPluginClasspath()
            .build();

    BuildTask task = result.task(":" + EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME);
    Assert.assertEquals(TaskOutcome.SUCCESS, task.getOutcome());
  }

  @Test
  public void testGenerateVisitors_ignoreWrongFileExtention() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis", "pkgZ");
    File tempSourceFile = new File(tempSourceDir, "Other.txt");
    tempSourceFile.createNewFile();

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME)
            .withPluginClasspath()
            .build();

    BuildTask task = result.task(":" + EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME);
    Assert.assertEquals(TaskOutcome.SUCCESS, task.getOutcome());

    File expectedGenFile =
        new File(tempProjectDir.getRoot() + "/build/generated-src/enumvis/pkgZ", "Other.txt");
    Assert.assertFalse(expectedGenFile.exists());
  }

  @Test
  public void testGenerateVisitors_withEmptyEnumName() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis", "pkgZ");
    writeVisitorDefinition(tempSourceDir, "");

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail();

    String expectedOutput = "The enum name is empty";
    Assert.assertTrue(
        "Build result output does not contain expected string: " + expectedOutput,
        result.getOutput().contains(expectedOutput));
  }

  @Test
  public void testGenerateVisitors_withInvalidEnumName() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis", "pkgZ");
    writeVisitorDefinition(tempSourceDir, "A(1)");

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail();

    String expectedOutput = "is not a valid identifier for Java";
    Assert.assertTrue(
        "Build result output does not contain expected string: " + expectedOutput,
        result.getOutput().contains(expectedOutput));
  }

  @Test
  public void testGenerateVisitors_withEmptyEnumDefinition() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis", "pkgZ");
    writeVisitorDefinition(tempSourceDir, "EmptyEnum", "");

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail();

    String expectedOutput = "does not have any members";
    Assert.assertTrue(
        "Build result output does not contain expected string: " + expectedOutput,
        result.getOutput().contains(expectedOutput));
  }

  @Test
  public void testGenerateVisitors_withInvalidLocation() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis");
    writeVisitorDefinition(tempSourceDir, "ValidDefinition");

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail();

    String expectedOutput = "The location of this enum maps to the default Java package";
    Assert.assertTrue(
        "Build result output does not contain expected string: " + expectedOutput,
        result.getOutput().contains(expectedOutput));
  }

  @Test
  public void testGenerateVisitors_withInvalidEnumMember() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis", "pkgZ");
    String visitorDefinition = """
        Invalid!Member
        """;
    writeVisitorDefinition(tempSourceDir, "InvalidMember", visitorDefinition);

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail();

    String expectedOutput = "is not a valid identifier for Java";
    Assert.assertTrue(
        "Build result output does not contain expected string: " + expectedOutput,
        result.getOutput().contains(expectedOutput));
  }

  @Test
  public void testGenerateVisitors_withDuplicateEnumMember() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis", "pkgZ");
    String visitorDefinition =
        """
        ThisIsADuplicate
        ThisIsUnique
        ThisIsADuplicate
        """;
    writeVisitorDefinition(tempSourceDir, "DuplicateMember", visitorDefinition);

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail();

    String expectedOutput = "Duplicate line in file";
    Assert.assertTrue(
        "Build result output does not contain expected string: " + expectedOutput,
        result.getOutput().contains(expectedOutput));
  }

  @Test
  public void testGenerateVisitors_withValidDefinition() throws IOException {
    File tempSourceDir = tempProjectDir.newFolder("src", "main", "enumvis", "pkgA", "pkg1");

    String testEnumName = "Planet";
    File tempSourceFile = new File(tempSourceDir, testEnumName + VISITOR_FILE_EXT);
    tempSourceFile.createNewFile();

    String visitorDefinition =
        """
        Mercury
        Venus
        Earth
        Mars
        Jupiter
        Saturn
        Uranus
        Neptune
        """;
    Files.write(tempSourceFile.toPath(), visitorDefinition.getBytes());

    BuildResult result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.getRoot())
            .withArguments(JavaPlugin.COMPILE_JAVA_TASK_NAME)
            .withPluginClasspath()
            .forwardOutput()
            .build();

    BuildTask task = result.task(":" + EnumVisitorPlugin.GENERATE_ENUM_VISITOR_TASK_NAME);
    Assert.assertEquals(TaskOutcome.SUCCESS, task.getOutcome());

    File expectedGenFile =
        new File(
            tempProjectDir.getRoot() + "/build/generated-src/enumvis/pkgA/pkg1",
            testEnumName + JAVA_FILE_EXT);
    verifyJavaFile(expectedGenFile);
  }

  private static void writeVisitorDefinition(File directory, String baseFileName)
      throws IOException {
    String visitorDefinition = """
        ValidMember
        """;
    writeVisitorDefinition(directory, baseFileName, visitorDefinition);
  }

  private static void writeVisitorDefinition(
      File directory, String baseFileName, String visitorDefinition) throws IOException {
    File tempSourceFile = new File(directory, baseFileName + VISITOR_FILE_EXT);
    tempSourceFile.createNewFile();
    Files.write(tempSourceFile.toPath(), visitorDefinition.getBytes());
  }

  private static void verifyJavaFile(File javaFile) throws IOException {
    Assert.assertTrue("The file must exist -- " + javaFile.toString(), javaFile.exists());

    String fileName = javaFile.getName();

    Assert.assertTrue(
        "The file name must end with '" + JAVA_FILE_EXT + "'", fileName.endsWith(JAVA_FILE_EXT));

    String enumName = fileName.replace(JAVA_FILE_EXT, "");

    Stream<String> fileLines = Files.lines(javaFile.toPath());
    String fileContents = fileLines.collect(Collectors.joining(LINE_SEPARATOR));
    fileLines.close();

    String expectedPackage = "package ";
    Assert.assertTrue(
        "The file contents must contain '" + expectedPackage + "'",
        fileContents.contains(expectedPackage));

    String expectedEnum = "public enum " + enumName;
    Assert.assertTrue(
        "The file contents must contain '" + expectedEnum + "'",
        fileContents.contains(expectedEnum));

    String expectedInterface = "public interface " + enumName + "Visitor";
    Assert.assertTrue(
        "The file contents must contain '" + expectedInterface + "'",
        fileContents.contains(expectedInterface));
  }
}
