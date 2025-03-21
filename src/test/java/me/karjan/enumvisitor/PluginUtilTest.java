package me.karjan.enumvisitor;

import org.junit.Assert;
import org.junit.Test;

/** Automated unit tests for {@link PluginUtil}. */
public class PluginUtilTest {

  @Test
  public void test_isEmpty_withNull() {
    Assert.assertTrue(PluginUtil.isEmpty(null));
  }

  @Test
  public void test_isEmpty_withZeroLength() {
    Assert.assertTrue(PluginUtil.isEmpty(""));
  }

  @Test
  public void test_isEmpty_withBlankString() {
    Assert.assertTrue(PluginUtil.isEmpty(" "));
  }

  @Test
  public void test_isEmpty_withNonBlankString() {
    Assert.assertFalse(PluginUtil.isEmpty("test"));
  }

  @Test
  public void test_isEmpty_withWhitespace() {
    Assert.assertTrue(PluginUtil.isEmpty("\t"));
  }

  @Test
  public void test_isEmpty_withNonBlankStringSurroundedByWhitespace() {
    Assert.assertFalse(PluginUtil.isEmpty("  test  "));
  }
}
