package me.karjan.enumvisitorgenerator;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: javadoc
 */
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
  public void test_isEmpty_withNonBlankStringSurroundedByWhitespace() {
    Assert.assertFalse(PluginUtil.isEmpty("  test  "));
  }

}
