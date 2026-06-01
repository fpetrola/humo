package ar.net.fpetrola.humo.lsp;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class ExecutionContext {
  public List<VariableInfo> getPreviousVars() {
    ArrayList<VariableInfo> variableInfos = new ArrayList<>();
    variableInfos.add(new VariableInfo(0, 30, "var1", "summary of var1"));
    variableInfos.add(new VariableInfo(32, 56, "var2", "summary of var2"));
    return variableInfos;
  }

  public List<VariableInfo> getNextVars() {
    ArrayList<VariableInfo> variableInfos = new ArrayList<>();
    variableInfos.add(new VariableInfo(200, 210, "var3", "summary of var3"));
    variableInfos.add(new VariableInfo(212, 216, "var4", "summary of var4"));
    return variableInfos;
  }

  public Range getCurrentActiveRange() {
    Range range = new Range(new Position(30, 0), new Position(60, 0));

    return range;
  }
}
