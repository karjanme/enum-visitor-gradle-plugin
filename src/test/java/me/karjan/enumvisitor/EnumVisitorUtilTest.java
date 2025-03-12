package me.karjan.enumvisitor;

import org.junit.Assert;
import org.junit.Test;

/** Automated unit tests for {@link EnumVisitorUtil}. */
public class EnumVisitorUtilTest {

  @Test
  public void test_isEmpty_withNull() {
    Assert.assertTrue(EnumVisitorUtil.isEmpty(null));
  }

  @Test
  public void test_isEmpty_withZeroLength() {
    Assert.assertTrue(EnumVisitorUtil.isEmpty(""));
  }

  @Test
  public void test_isEmpty_withBlankString() {
    Assert.assertTrue(EnumVisitorUtil.isEmpty(" "));
  }

  @Test
  public void test_isEmpty_withNonBlankString() {
    Assert.assertFalse(EnumVisitorUtil.isEmpty("test"));
  }

  @Test
  public void test_isEmpty_withWhitespace() {
    Assert.assertTrue(EnumVisitorUtil.isEmpty("\t"));
  }

  @Test
  public void test_isEmpty_withNonBlankStringSurroundedByWhitespace() {
    Assert.assertFalse(EnumVisitorUtil.isEmpty("  test  "));
  }
}
