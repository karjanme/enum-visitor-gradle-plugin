package me.karjan.enumvisitorgenerator;

import java.nio.file.FileSystems;

/**
 * TODO: javadoc
 */
class PluginUtil {

  static final String DEFAULT_SOURCE_DIRECTORY = "src/main/visgen";
  static final String VISITOR_FILE_EXT = ".v";
  static final String JAVA_FILE_EXT = ".java";

  static final String BLANK_SPACE = " ";
  static final String LINE_SEPARATOR = System.lineSeparator();
  static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();
  static final String PACKAGE_SEPARATOR = ".";

  static final String JAVA_IDENTIFIER_REGEX = "^([a-zA-Z_$][a-zA-Z\\d_$]*)$";

  /**
   * TODO: javadoc
   */
  static boolean isEmpty(String s) {
    return (s == null || s.isBlank());
  }

}
