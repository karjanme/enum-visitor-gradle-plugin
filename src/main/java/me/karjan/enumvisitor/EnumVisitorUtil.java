package me.karjan.enumvisitor;

import java.nio.file.FileSystems;

/** Constants and Helper methods for the Plugin. */
class EnumVisitorUtil {

  /** The default source directory for the definitions. */
  static final String DEFAULT_SOURCE_DIRECTORY = "src/main/enumvis";

  /** The file extension for a definition. */
  static final String VISITOR_FILE_EXT = ".v";

  /** The file extension for Java source code. */
  static final String JAVA_FILE_EXT = ".java";

  static final String BLANK_SPACE = " ";
  static final String PACKAGE_SEPARATOR = ".";

  static final String LINE_SEPARATOR = System.lineSeparator();
  static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

  /** Regular expression for a valid Java identifier. */
  static final String JAVA_IDENTIFIER_REGEX = "^([a-zA-Z_$][a-zA-Z\\d_$]*)$";

  private EnumVisitorUtil() {
    // Do not instantiate
  }

  /** Returns true if the given string is empty, meaning it is null or contains only whitespace. */
  static boolean isEmpty(String s) {
    return (s == null || s.isBlank());
  }
}
