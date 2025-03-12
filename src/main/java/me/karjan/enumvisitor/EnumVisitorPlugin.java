package me.karjan.enumvisitor;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;

/** Specific implementation of a Gradle Plugin for the Enum Visitor generator. */
public final class EnumVisitorPlugin implements Plugin<Project> {

  /** The name of the Enum Visitor generation task. */
  public static final String GENERATE_ENUM_VISITOR_TASK_NAME = "generateEnumVisitors";

  @Override
  public void apply(final Project target) {

    target.getPlugins().apply(JavaPlugin.class);

    TaskContainer tasks = target.getTasks();

    GenerateEnumVisitorsTask ourPluginTask =
        tasks.register(GENERATE_ENUM_VISITOR_TASK_NAME, GenerateEnumVisitorsTask.class).get();

    ExtensionContainer extensions = target.getExtensions();
    SourceSetContainer sourceSets = extensions.getByType(SourceSetContainer.class);

    sourceSets.named(
        SourceSet.MAIN_SOURCE_SET_NAME,
        sourceSet -> {
          sourceSet.getJava().srcDir(ourPluginTask.getDestination());
        });

    tasks.named(
        JavaPlugin.COMPILE_JAVA_TASK_NAME,
        task -> {
          task.dependsOn(ourPluginTask);
        });
  }
}
