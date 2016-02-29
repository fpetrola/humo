package ar.net.fpetrola.humo.serverside;

public class HighlighterTester
{
//	@Test
//	public void insertingTextShouldReturnTheSameText()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "text1");
//
//		assertEquals("text1", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void insertingTextAtPosition4ShouldReturnPlainTextBeginningWith3Spaces()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(4, "text1");
//
//		assertEquals("    text1", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void removingWholeTextShouldReturnEmptyString()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(4, "text1");
//		textHighlighter.delete(0, 9);
//
//		assertEquals("", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void removingPartialTextFromBeginningShouldReturnTail()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(4, "text1");
//		textHighlighter.delete(0, 6);
//
//		assertEquals("xt1", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void removingPartialTextInsideHighlightedRangeShouldReturnTail()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("red", 3, 18);
//		assertEquals("thi<span class='red'>s is a text to </span>be highlighted", textHighlighter.getResultingText());
//		textHighlighter.delete(6, 10);
//		assertEquals("thi<span class='red'>s itext to </span>be highlighted", textHighlighter.getResultingText());
//	}
//	
//	@Test
//	public void removingPartialTextOutsideAndInsideHighlightedRangeShouldRemoveBothParts()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("red", 3, 18);
//		assertEquals("thi<span class='red'>s is a text to </span>be highlighted", textHighlighter.getResultingText());
//		textHighlighter.delete(2, 10);
//		assertEquals("th<span class='red'>ext to </span>be highlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingFirstCharactersShouldReturnSpanPlusPlainText()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "text1");
//		textHighlighter.highlight("red", 0, 3);
//
//		assertEquals("<span class='red'>tex</span>t1", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingLastCharactersShouldReturnPlainTextPlusSpan()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "text1");
//		textHighlighter.highlight("red", 3, 5);
//
//		assertEquals("tex<span class='red'>t1</span>", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void insertingInsideHightlightedRangeShouldExpandSpan()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("red", 3, 18);
//		assertEquals("thi<span class='red'>s is a text to </span>be highlighted", textHighlighter.getResultingText());
//		textHighlighter.insert(10, "[other text]");
//
//		assertEquals("thi<span class='red'>s is a [other text]text to </span>be highlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void insertingAfterHightlightedRangeShouldAppendText()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("red", 3, 18);
//		assertEquals("thi<span class='red'>s is a text to </span>be highlighted", textHighlighter.getResultingText());
//		textHighlighter.insert(22, "[other text]");
//
//		assertEquals("thi<span class='red'>s is a text to </span>be h[other text]ighlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void insertingInsideHightlightedRangeTwiceShouldExpandSpan()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("red", 3, 18);
//		assertEquals("thi<span class='red'>s is a text to </span>be highlighted", textHighlighter.getResultingText());
//		textHighlighter.insert(10, "[first text]");
//		assertEquals("thi<span class='red'>s is a [first text]text to </span>be highlighted", textHighlighter.getResultingText());
//		textHighlighter.insert(25, "[second text]");
//		assertEquals("thi<span class='red'>s is a [first text]tex[second text]t to </span>be highlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void insertingTextBetweenTwoHightlightedRangesShouldMoveSecondSpan()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("red", 3, 8);
//		textHighlighter.highlight("blue", 12, 20);
//		assertEquals("thi<span class='red'>s is </span>a te<span class='blue'>xt to be</span> highlighted", textHighlighter.getResultingText());
//
//		textHighlighter.insert(10, "[other text]");
//		assertEquals("thi<span class='red'>s is </span>a [other text]te<span class='blue'>xt to be</span> highlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingTwoConsecutivePartsShouldReturnTwoSpans()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("red", 3, 8);
//		textHighlighter.highlight("blue", 12, 20);
//
//		assertEquals("thi<span class='red'>s is </span>a te<span class='blue'>xt to be</span> highlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingTwoConsecutiveInInverseOrderPartsShouldReturnTwoSpans()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("blue", 12, 20);
//		textHighlighter.highlight("red", 3, 8);
//
//		assertEquals("thi<span class='red'>s is </span>a te<span class='blue'>xt to be</span> highlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingThreeConsecutiveIn213OrderPartsShouldReturnThreeSpans()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("blue", 12, 20);
//		textHighlighter.highlight("red", 3, 8);
//		textHighlighter.highlight("green", 23, 27);
//
//		assertEquals("thi<span class='red'>s is </span>a te<span class='blue'>xt to be</span> hi<span class='green'>ghli</span>ghted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingInsideHighlightedRangeShouldReturnNestedSpans()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("blue", 5, 20);
//		assertEquals("this <span class='blue'>is a text to be</span> highlighted", textHighlighter.getResultingText());
//		textHighlighter.highlight("red", 10, 15);
//
//		assertEquals("this <span class='blue'>is a <span class='red'>text </span>to be</span> highlighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingAlreadyHighlightedRangeShouldReturnExpandRange()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("blue", 5, 20);
//		assertEquals("this <span class='blue'>is a text to be</span> highlighted", textHighlighter.getResultingText());
//		textHighlighter.highlight("blue", 7, 25);
//
//		assertEquals("this <span class='blue'>is a text to be high</span>lighted", textHighlighter.getResultingText());
//	}
//
//	@Test
//	public void hightlightingMultipleTimesAlreadyHighlightedRangeShouldReturnExpandRange()
//	{
//		TextHighlighter textHighlighter= new TextHighlighter();
//		textHighlighter.insert(0, "this is a text to be highlighted");
//		textHighlighter.highlight("blue", 0, 5);
//		assertEquals("<span class='blue'>this </span>is a text to be highlighted", textHighlighter.getResultingText());
//		textHighlighter.highlight("blue", 0, 6);
//		assertEquals("<span class='blue'>this i</span>s a text to be highlighted", textHighlighter.getResultingText());
//		textHighlighter.highlight("blue", 0, 7);
//		assertEquals("<span class='blue'>this is</span> a text to be highlighted", textHighlighter.getResultingText());
//	}

}
