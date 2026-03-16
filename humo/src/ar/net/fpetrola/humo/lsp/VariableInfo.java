package ar.net.fpetrola.humo.lsp;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class VariableInfo {
  private Range range;
  private String name;
  private String summary;

  public VariableInfo(int i, int i1, String name, String summary) {
    range = new Range(new Position(i, 0), new Position(i1, 0));
    this.name = name;
    this.summary = summary;
  }

  public Range getRange() {
    return range;
  }

  public String getName() {
    return name;
  }

  public String getSummary() {
    return summary;
  }
}
