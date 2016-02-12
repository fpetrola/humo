import static org.junit.Assert.*;

import org.junit.Test;

public class Test1 {

  @Test
  public void insertingTextAtPosition0ShouldRenderSamePlainText() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(0, "text1");
    String result = textDocument.getRenderedText();

    assertEquals("text1", result);
  }

  @Test
  public void insertingTextAtPosition1ShouldRenderSamePlainTextAtPosition1() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "text1");
    String result = textDocument.getRenderedText();

    assertEquals(" text1", result);
  }

  @Test
  public void wrappingTextFrom1To5ShouldRenderSpan() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text");
    textDocument.setSpan("red", 3, 7);
    String result = textDocument.getRenderedText();

    assertEquals(" th<span class=\"red\">is_i</span>s_a_text", result);
  }

  @Test
  public void wrappingTextTwoConsecutiveTimesShouldRenderTwoConsecutiveSpans() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("red", 3, 7);
    String result = textDocument.getRenderedText();
    assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped",
        result);

    textDocument.setSpan("blue", 12, 19);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);
  }

  @Test
  public void wrappingTextTwoConsecutiveTimesInInverseOrderShouldRenderTwoConsecutiveSpans() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("blue", 12, 19);
    String result = textDocument.getRenderedText();
    assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.setSpan("red", 3, 7);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);
  }

  @Test
  public void wrappingTextThreeConsecutiveTimesInInverseOrderShouldRenderThreeConsecutiveSpansIn_132_Order() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("red", 3, 7);
    String result = textDocument.getRenderedText();
    assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped",
        result);

    textDocument.setSpan("green", 23, 26);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_text_to_be_w<span class=\"green\">rap</span>ped",
        result);

    textDocument.setSpan("blue", 12, 19);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_w<span class=\"green\">rap</span>ped",
        result);
  }

  @Test
  public void wrappingTextThreeConsecutiveTimesInInverseOrderShouldRenderThreeConsecutiveSpansIn_213_Order() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("blue", 12, 19);
    String result = textDocument.getRenderedText();
    assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.setSpan("red", 3, 7);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.setSpan("green", 23, 26);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_w<span class=\"green\">rap</span>ped",
        result);
  }

  @Test
  public void removingTextWrappingShouldRenderPlainText() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(0, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("blue", 12, 19);
    String result = textDocument.getRenderedText();
    assertEquals("this_is_a_te<span class=\"blue\">xt_to_b</span>e_wrapped",
        result);

    textDocument.removeSpan(12, 19);
    result = textDocument.getRenderedText();
    assertEquals("this_is_a_text_to_be_wrapped", result);
  }

  @Test
  public void removingTwoTextWrappingInInverseOrderShouldRenderPlainText() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("blue", 12, 19);
    String result = textDocument.getRenderedText();
    assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.setSpan("red", 3, 7);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.removeSpan(12, 19);
    textDocument.removeSpan(3, 7);
    result = textDocument.getRenderedText();

    assertEquals(" this_is_a_text_to_be_wrapped", result);
  }

  @Test
  public void removingAnExternalRangeOfTwoTextWrappingShouldRenderPlainText() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("blue", 12, 19);

    String result = textDocument.getRenderedText();
    assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.setSpan("red", 3, 7);

    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.removeSpan(3, 19);

    result = textDocument.getRenderedText();
    assertEquals(" this_is_a_text_to_be_wrapped", result);
  }

  @Test
  public void wrappingAWrappersShouldRenderNestedWrappers() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("red", 3, 7);
    String result = textDocument.getRenderedText();
    assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped",
        result);

    textDocument.setSpan("green", 2, 25);
    result = textDocument.getRenderedText();
    assertEquals(
        " t<span class=\"green\">h<span class=\"red\">is_i</span>s_a_text_to_be_wra</span>pped",
        result);
  }

  @Test
  public void wrappingTwoWrappersShouldRenderNestedWrappers() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(1, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("red", 3, 7);
    String result = textDocument.getRenderedText();
    assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped",
        result);

    textDocument.setSpan("blue", 12, 19);
    result = textDocument.getRenderedText();
    assertEquals(
        " th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped",
        result);

    textDocument.setSpan("green", 2, 25);
    result = textDocument.getRenderedText();
    assertEquals(
        " t<span class=\"green\">h<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wra</span>pped",
        result);
  }

  @Test
  public void wrappingAWrapperPartiallyAtTheBeginningShouldSplitWrapper() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(0, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("red", 12, 20);
    String result = textDocument.getRenderedText();
    assertEquals("this_is_a_te<span class=\"red\">xt_to_be</span>_wrapped",
        result);

    textDocument.setSpan("green", 6, 16);
    result = textDocument.getRenderedText();
    assertEquals(
        "this_i<span class=\"green\">s_a_text_t</span><span class=\"red\">o_be</span>_wrapped",
        result);
  }

  @Test
  public void wrappingAWrapperPartiallyAtTheEndShouldSplitWrapper() {

    DefaultTextDocument textDocument = new DefaultTextDocument();
    textDocument.insert(0, "this_is_a_text_to_be_wrapped");

    textDocument.setSpan("red", 12, 20);
    String result = textDocument.getRenderedText();
    assertEquals("this_is_a_te<span class=\"red\">xt_to_be</span>_wrapped",
        result);

    textDocument.setSpan("green", 16, 25);
    result = textDocument.getRenderedText();
    assertEquals(
        "this_is_a_te<span class=\"red\">xt_t</span><span class=\"green\">o_be_wrap</span>ped",
        result);
  }

}
