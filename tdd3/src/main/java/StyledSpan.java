
public class StyledSpan {
  private int start;
  private int end;
  private String style;
  private int replacementStart;
  public void setReplacementStart(int replacementStart) {
    this.replacementStart = replacementStart;
  }

  public void setReplacementEnd(int replacementEnd) {
    this.replacementEnd = replacementEnd;
  }

  public void setReplacementText(String replacementText) {
    this.replacementText = replacementText;
  }

  private int replacementEnd;
  private String replacementText;

  public StyledSpan(String style, int start, int end) {
    this.style = style;
    this.start = start;
    this.end = end;
  }

  public int getEnd() {
    return end;
  }

  public int getReplacementEnd() {
    return replacementEnd;
  }

  public int getReplacementStart() {
    return replacementStart;
  }

  public String getReplacementText() {
    return replacementText;
  }

  public int getStart() {
    return start;
  }

  public String getStyle() {
    return style;
  }
}
