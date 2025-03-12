package me.karjan.enumvisitorgenerator;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;

/**
 * TODO: javadoc
 */
public class PluginImpl implements Plugin<Project> {

  static final String VISITOR_GENERATOR_TASK_NAME = "generateVisitors";

  @Override
  public void apply(final Project target) {

    target.getPluginManager().apply(JavaPlugin.class);

    TaskContainer tasks = target.getTasks();

    VisitorGeneratorTask ourPluginTask = tasks.register(VISITOR_GENERATOR_TASK_NAME, VisitorGeneratorTask.class).get();

    target.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
      @Override
      public void execute(final JavaPlugin plugin) {
        // TODO: still not working like this?
          SourceSetContainer sourceSets = (SourceSetContainer) target.getProperties().get("sourceSets");
          sourceSets.getByName("main").getJava().getSrcDirs().add(ourPluginTask.getDestination());

          tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(ourPluginTask);
      }
    });

  }
    
}
