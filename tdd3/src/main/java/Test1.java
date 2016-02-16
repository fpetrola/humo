import static org.junit.Assert.*;

import org.junit.Test;

public class Test1
{

    @Test
    public void insertingTextAtPosition0ShouldRenderSamePlainText()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "text1");
	String result= textDocument.getRenderedText();

	assertEquals("text1", result);
    }

    @Test
    public void deletingExistingTextAtPosition0To5ShouldRenderSamePlainTextFrom6()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "a_text_to_be_deleted");
	textDocument.delete(0, 5);
	String result= textDocument.getRenderedText();

	assertEquals("t_to_be_deleted", result);
    }

    @Test
    public void deletingExistingWrappedTextShouldRenderNothing()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "a_text_to_be_deleted");
	textDocument.setSpan("red", 4, 10);
	textDocument.delete(0, 23);
	String result= textDocument.getRenderedText();

	assertEquals("", result);
    }

    //    @Test
    //    public void deletingExistingWrappedTextPartiallyAtTheBeggingShouldRenderTheRemainingEndOfWrappedText()
    //    {
    //	DefaultTextDocument textDocument= new DefaultTextDocument();
    //	textDocument.insert(0, "a_text_to_be_deleted");
    //	StyledSpan span= textDocument.setSpan("red", 4, 10);
    //	textDocument.delete(0, 6);
    //	String result= textDocument.getRenderedText();
    //
    //	assertEquals("<span class=\"red\">_to_</span>be_deleted", result);
    //	assertEquals(0, span.getStart());
    //	assertEquals(8, span.getEnd());
    //    }

    @Test
    public void insertingTextAtPosition1ShouldRenderSamePlainTextAtPosition1()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "text1");
	String result= textDocument.getRenderedText();

	assertEquals(" text1", result);
    }

    @Test
    public void wrappingTextFrom1To5ShouldRenderSpan()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text");
	textDocument.setSpan("red", 3, 7);
	String result= textDocument.getRenderedText();

	assertEquals(" th<span class=\"red\">is_i</span>s_a_text", result);
    }

    @Test
    public void insertingTextIntoWrappedTextShouldExtendWrapping()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text");
	StyledSpan span= textDocument.setSpan("red", 3, 7);
	textDocument.insert(5, "-new-content-");

	String result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is-new-content-_i</span>s_a_text", result);

	assertEquals(3, span.getStart());
	assertEquals(7 + 13, span.getEnd());
	assertEquals("is-new-content-_i", span.getReplacementText());
    }

    @Test
    public void wrappingAfterInsertingShouldExtendFirstWrappingAndCreateSecond()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text");
	textDocument.setSpan("red", 3, 7);
	textDocument.insert(5, "-new-content-");
	String result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is-new-content-_i</span>s_a_text", result);

	textDocument.setSpan("blue", 9, 12);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is-new<span class=\"blue\">-co</span>ntent-_i</span>s_a_text", result);
    }

    @Test
    public void insertingTextIntoTwoTimesWrappedTextShouldExtendWrappers()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "this_is_a_text_to_be_wrapped");
	textDocument.setSpan("red", 3, 20);
	String result= textDocument.getRenderedText();
	assertEquals("thi<span class=\"red\">s_is_a_text_to_be</span>_wrapped", result);
	textDocument.setSpan("blue", 8, 16);
	result= textDocument.getRenderedText();
	assertEquals("thi<span class=\"red\">s_is_<span class=\"blue\">a_text_t</span>o_be</span>_wrapped", result);
	textDocument.insert(10, "-new-content-");
	result= textDocument.getRenderedText();
	assertEquals("thi<span class=\"red\">s_is_<span class=\"blue\">a_-new-content-text_t</span>o_be</span>_wrapped", result);
    }

    @Test
    public void wrappingTextTwoConsecutiveTimesShouldRenderTwoConsecutiveSpans()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 3, 7);
	String result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped", result);

	textDocument.setSpan("blue", 12, 19);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);
    }

    @Test
    public void wrappingTextTwoConsecutiveTimesInInverseOrderShouldRenderTwoConsecutiveSpans()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("blue", 12, 19);
	String result= textDocument.getRenderedText();
	assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.setSpan("red", 3, 7);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);
    }

    @Test
    public void wrappingTextThreeConsecutiveTimesInInverseOrderShouldRenderThreeConsecutiveSpansIn_132_Order()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 3, 7);
	String result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped", result);

	textDocument.setSpan("green", 23, 26);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_w<span class=\"green\">rap</span>ped", result);

	textDocument.setSpan("blue", 12, 19);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_w<span class=\"green\">rap</span>ped", result);
    }

    @Test
    public void wrappingTextThreeConsecutiveTimesInInverseOrderShouldRenderThreeConsecutiveSpansIn_213_Order()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("blue", 12, 19);
	String result= textDocument.getRenderedText();
	assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.setSpan("red", 3, 7);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.setSpan("green", 23, 26);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_w<span class=\"green\">rap</span>ped", result);
    }

    @Test
    public void removingTextWrappingShouldRenderPlainText()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("blue", 12, 19);
	String result= textDocument.getRenderedText();
	assertEquals("this_is_a_te<span class=\"blue\">xt_to_b</span>e_wrapped", result);

	textDocument.removeSpansBetween(12, 19);
	result= textDocument.getRenderedText();
	assertEquals("this_is_a_text_to_be_wrapped", result);
    }

    @Test
    public void removingTwoTextWrappingShouldRenderPlainText()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 3, 7);
	String result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped", result);

	StyledSpan blueSpan= textDocument.setSpan("blue", 12, 19);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);
	assertEquals(37, blueSpan.getReplacementStart());
	assertEquals(70, blueSpan.getReplacementEnd());

	textDocument.removeSpansBetween(3, 7);
	result= textDocument.getRenderedText();
	assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);
	assertEquals(12, blueSpan.getReplacementStart());
	assertEquals(47, blueSpan.getReplacementEnd());
    }
    
    @Test
    public void removingTwoTextWrappingInInverseOrderShouldRenderPlainText()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("blue", 12, 19);
	String result= textDocument.getRenderedText();
	assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.setSpan("red", 3, 7);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.removeSpansBetween(12, 19);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped", result);

	textDocument.removeSpansBetween(3, 7);
	result= textDocument.getRenderedText();

	assertEquals(" this_is_a_text_to_be_wrapped", result);
    }

    @Test
    public void removingAnExternalRangeOfTwoTextWrappingShouldRenderPlainText()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("blue", 12, 19);

	String result= textDocument.getRenderedText();
	assertEquals(" this_is_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.setSpan("red", 3, 7);

	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.removeSpansBetween(3, 19);

	result= textDocument.getRenderedText();
	assertEquals(" this_is_a_text_to_be_wrapped", result);
    }

    @Test
    public void wrappingAWrappersShouldRenderNestedWrappers()
    {
	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 3, 7);
	String result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped", result);

	textDocument.setSpan("green", 2, 25);
	result= textDocument.getRenderedText();
	assertEquals(" t<span class=\"green\">h<span class=\"red\">is_i</span>s_a_text_to_be_wra</span>pped", result);
    }

    @Test
    public void wrappingTwoWrappersShouldRenderNestedWrappers()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(1, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 3, 7);
	String result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_text_to_be_wrapped", result);

	textDocument.setSpan("blue", 12, 19);
	result= textDocument.getRenderedText();
	assertEquals(" th<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wrapped", result);

	textDocument.setSpan("green", 2, 25);
	result= textDocument.getRenderedText();
	assertEquals(" t<span class=\"green\">h<span class=\"red\">is_i</span>s_a_t<span class=\"blue\">ext_to_</span>be_wra</span>pped", result);
    }

    @Test
    public void wrappingAWrapperPartiallyAtTheBeginningShouldSplitWrapper()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 12, 20);
	String result= textDocument.getRenderedText();
	assertEquals("this_is_a_te<span class=\"red\">xt_to_be</span>_wrapped", result);

	textDocument.setSpan("green", 6, 16);
	result= textDocument.getRenderedText();
	assertEquals("this_i<span class=\"green\">s_a_text_t</span><span class=\"red\">o_be</span>_wrapped", result);
    }

    @Test
    public void wrappingAWrapperPartiallyAtTheEndShouldSplitWrapper()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 12, 20);
	String result= textDocument.getRenderedText();
	assertEquals("this_is_a_te<span class=\"red\">xt_to_be</span>_wrapped", result);

	textDocument.setSpan("green", 16, 25);
	result= textDocument.getRenderedText();
	assertEquals("this_is_a_te<span class=\"red\">xt_t</span><span class=\"green\">o_be_wrap</span>ped", result);
    }

    @Test
    public void wrappingAWrapperPartiallyAtTheBeginningAndEndShouldAddWrapperInside()
    {

	DefaultTextDocument textDocument= new DefaultTextDocument();
	textDocument.insert(0, "this_is_a_text_to_be_wrapped");

	textDocument.setSpan("red", 12, 20);
	String result= textDocument.getRenderedText();
	assertEquals("this_is_a_te<span class=\"red\">xt_to_be</span>_wrapped", result);

	textDocument.setSpan("green", 16, 18);
	result= textDocument.getRenderedText();
	assertEquals("this_is_a_te<span class=\"red\">xt_t<span class=\"green\">o_</span>be</span>_wrapped", result);
    }

}
