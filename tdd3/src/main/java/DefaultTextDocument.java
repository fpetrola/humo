import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultTextDocument {

  private StringBuilder textBuffer = new StringBuilder();
  private List<StyledSpan> spans = new ArrayList<StyledSpan>();
  String spanOpeningTemplate = "<span class=\"%s\">";
  String spanClose = "</span>";

  public void insert(int start, String string) {
    if (textBuffer.length() < start)
      appendCharacters(start - textBuffer.length(), ' ');

    textBuffer.insert(start, string);
  }

  private void appendCharacters(final int numberOfChars, char aChar) {
    final char[] spaceArray = new char[numberOfChars];
    Arrays.fill(spaceArray, aChar);
    textBuffer.append(spaceArray);
  }

  public void setSpan(String style, int start, int end) {
    StyledSpan styledSpan = new StyledSpan(style, start, end);

    String spanOpening = String.format(spanOpeningTemplate,
        styledSpan.getStyle());

    int deltaEnd = spanClose.length();
    int spanOverhead = deltaEnd + spanOpening.length();

    int startPositionDisplacement = 0;
    int endPositionDisplacement = 0;

    List<StyledSpan> arrayList = new ArrayList<StyledSpan>(spans);
    for (StyledSpan storedStyledSpan : arrayList) {
      int deltaStart = storedStyledSpan.getStyle().length()
          + spanOpeningTemplate.length() - 2;

      if (storedStyledSpan.getStart() != start
          && storedStyledSpan.getEnd() < end)
        endPositionDisplacement += deltaStart + deltaEnd;

      if (storedStyledSpan.getStart() < start)
        startPositionDisplacement += deltaStart + deltaEnd;

      if (storedStyledSpan.getStart() < end
          && storedStyledSpan.getEnd() > end) {
        removeSpan(storedStyledSpan.getStart(), storedStyledSpan.getEnd());
        setSpan(storedStyledSpan.getStyle(), end, storedStyledSpan.getEnd());
      }

      updateOlderReplacements(spanOverhead, storedStyledSpan);
    }

    createReplacement(start, end, styledSpan, spanOpening, spanOverhead,
        startPositionDisplacement, endPositionDisplacement);

    spans.add(styledSpan);
  }

  private void createReplacement(int start, int end, StyledSpan styledSpan,
      String spanOpening, int spanOverhead, int startPositionDisplacement,
      int endPositionDisplacement) {
    int replacementStart = styledSpan.getStart() + startPositionDisplacement;
    int replacementEnd = styledSpan.getEnd() + endPositionDisplacement;

    styledSpan.setReplacementStart(replacementStart);
    styledSpan.setReplacementEnd(replacementEnd + spanOverhead);
    styledSpan.setReplacementText(
        textBuffer.substring(replacementStart, replacementStart + end - start));

    textBuffer.insert(replacementEnd, spanClose);
    textBuffer.insert(replacementStart, spanOpening);
  }

  private void updateOlderReplacements(int spanOverhead,
      StyledSpan storedStyledSpan) {
    storedStyledSpan.setReplacementStart(
        storedStyledSpan.getReplacementStart() + spanOverhead);

    storedStyledSpan
        .setReplacementEnd(storedStyledSpan.getReplacementEnd() + spanOverhead);
  }

  public String getRenderedText() {
    return textBuffer.toString();
  }

  public void removeSpan(int start, int end) {
    List<StyledSpan> arrayList = new ArrayList<StyledSpan>(spans);
    for (StyledSpan storedStyledSpan : arrayList) {
      if (start <= storedStyledSpan.getStart()
          && end >= storedStyledSpan.getEnd()) {
        textBuffer.replace(storedStyledSpan.getReplacementStart(),
            storedStyledSpan.getReplacementEnd(),
            storedStyledSpan.getReplacementText());

        spans.remove(storedStyledSpan);
      }
    }
  }
}
