package ar.net.fpetrola.humo.lsp;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Step-by-step Humo interpreter that tracks definition positions.
 * The source mutates in-place. Each production remembers WHERE in the
 * source it was last defined (the offset range of pattern(production)).
 */
public class SteppableHumoInterpreter
{

    public static class StackFrame
    {
        public final int depth;
        public final String pattern;
        public final String production;
        public final int id;

        StackFrame(int depth, String pattern, String production, int id)
        {
            this.depth = depth;
            this.pattern = pattern;
            this.production = production;
            this.id = id;
        }

        @Override
        public String toString()
        {
            if (pattern.isEmpty()) return "(root)";
            String p = production.length() > 60 ? production.substring(0, 60) + "..." : production;
            return pattern + " → " + p;
        }
    }

    /** Where a production was defined in the source. */
    public static class DefLocation
    {
        public final int patternStart; // start of pattern text
        public final int braceOpen;    // position of {
        public final int braceClose;   // position of }
        public final String value;     // the production value

        DefLocation(int patternStart, int braceOpen, int braceClose, String value)
        {
            this.patternStart = patternStart;
            this.braceOpen = braceOpen;
            this.braceClose = braceClose;
            this.value = value;
        }
    }

    public interface StepListener
    {
        /**
         * Called before a substitution. The sourcecode is the MAIN source (mutating).
         * defLocation tells where in the source this production was defined.
         */
        void onBeforeSubstitute(String sourcecode, String pattern, String production,
                int matchStart, int matchEnd,
                DefLocation defLocation, List<StackFrame> stack);

        void onAfterSubstitute(String sourcecode, int insertStart, int insertEnd,
                List<StackFrame> stack);

        void onFinished(String sourcecode);

        // Parser scanning: current..last is the token being tested against productions.
        default void onScan(String sourcecode, int current, int last, List<StackFrame> stack) {}

        /** A definition pattern(production) was created/updated. */
        default void onDefinition(String sourcecode, String pattern, String production,
                int patStart, int braceOpen, int braceClose,
                List<StackFrame> stack) {}
    }

    protected Map<String, String> productions = new HashMap<>();
    /** Track where each production was defined in the source. */
    protected Map<String, DefLocation> productionLocations = new HashMap<>();

    private StepListener listener;
    private volatile CountDownLatch latch;
    private volatile boolean running = true;

    public enum StepMode { STEP_INTO, STEP_OUT, RUN }
    private volatile StepMode stepMode = StepMode.STEP_INTO;
    private volatile int stepOutTargetDepth = -1;

    private final List<StackFrame> stack = new ArrayList<>();
    private int frameIdCounter = 0;
    private int currentDepth = 0;

    // — Breakpoints —
    // Each breakpoint is an offset in the CURRENT (mutating) source.
    // When a replace happens before a breakpoint, the offset is adjusted.
    private final List<Breakpoint> breakpoints = new ArrayList<>();
    private int breakpointIdCounter = 1;

    public static class Breakpoint {
        public final int id;
        public volatile int offset;        // current offset in mutating source
        public final int originalOffset;   // where it was set originally
        public volatile boolean verified;
        public volatile boolean hit;

        Breakpoint(int id, int offset) {
            this.id = id;
            this.offset = offset;
            this.originalOffset = offset;
            this.verified = true;
            this.hit = false;
        }
    }

    /**
     * Add a breakpoint at the given offset in the current source.
     * Returns the breakpoint object (with id).
     */
    public Breakpoint addBreakpoint(int offset) {
        Breakpoint bp = new Breakpoint(breakpointIdCounter++, offset);
        synchronized (breakpoints) { breakpoints.add(bp); }
        return bp;
    }

    /** Remove a breakpoint by id. */
    public void removeBreakpoint(int id) {
        synchronized (breakpoints) { breakpoints.removeIf(bp -> bp.id == id); }
    }

    /** Clear all breakpoints. */
    public void clearBreakpoints() {
        synchronized (breakpoints) { breakpoints.clear(); }
    }

    /** Get current breakpoints (snapshot). */
    public List<Breakpoint> getBreakpoints() {
        synchronized (breakpoints) { return new ArrayList<>(breakpoints); }
    }

    /**
     * Adjust all breakpoint offsets after a replace operation.
     * If a replace at [replaceStart..replaceEnd) inserts text of newLength,
     * breakpoints after replaceStart are shifted by (newLength - oldLength).
     */
    private void adjustBreakpoints(int replaceStart, int oldLength, int newLength) {
        int delta = newLength - oldLength;
        if (delta == 0) return;
        synchronized (breakpoints) {
            for (Breakpoint bp : breakpoints) {
                if (bp.offset > replaceStart) {
                    bp.offset += delta;
                    if (bp.offset < 0) bp.offset = 0;
                }
            }
        }
    }

    /**
     * Check if there's a breakpoint near the given position.
     * A breakpoint "hits" if the parser's current match range overlaps it.
     */
    private boolean isAtBreakpoint(int matchStart, int matchEnd) {
        synchronized (breakpoints) {
            for (Breakpoint bp : breakpoints) {
                if (bp.offset >= matchStart && bp.offset <= matchEnd) {
                    bp.hit = true;
                    return true;
                }
            }
        }
        return false;
    }

    // =====================

    public void setListener(StepListener listener) { this.listener = listener; }
    public Map<String, String> getProductions() { return productions; }
    public Map<String, DefLocation> getProductionLocations() { return productionLocations; }
    public List<StackFrame> getStack() { return Collections.unmodifiableList(new ArrayList<>(stack)); }
    public int getCurrentDepth() { return currentDepth; }

    public void resume()
    {
        stepMode = StepMode.STEP_INTO;
        CountDownLatch l = latch;
        if (l != null) l.countDown();
    }

    public void stepOut()
    {
        stepMode = StepMode.STEP_OUT;
        stepOutTargetDepth = Math.max(0, currentDepth - 1);
        CountDownLatch l = latch;
        if (l != null) l.countDown();
    }

    public void runAll()
    {
        stepMode = StepMode.RUN;
        CountDownLatch l = latch;
        if (l != null) l.countDown();
    }

    public void stop()
    {
        running = false;
        resume();
    }

    private boolean shouldPause()
    {
        return shouldPause(-1, -1);
    }

    private boolean shouldPause(int matchStart, int matchEnd)
    {
        if (!running) return false;
        // Always check breakpoints first (even in RUN mode)
        if (matchStart >= 0 && matchEnd >= 0 && isAtBreakpoint(matchStart, matchEnd))
        {
            stepMode = StepMode.STEP_INTO; // switch to stepping after hitting BP
            return true;
        }
        switch (stepMode)
        {
            case STEP_INTO: return true;
            case STEP_OUT:
                if (currentDepth <= stepOutTargetDepth)
                {
                    stepMode = StepMode.STEP_INTO;
                    return true;
                }
                return false;
            case RUN: return false;
            default: return true;
        }
    }

    private void waitForStep()
    {
        if (!running) return;
        latch = new CountDownLatch(1);
        try { latch.await(); } catch (InterruptedException e) { running = false; }
    }

    private void pushFrame(String pattern, String production)
    {
        currentDepth++;
        stack.add(new StackFrame(currentDepth, pattern, production, frameIdCounter++));
    }

    private void popFrame()
    {
        if (!stack.isEmpty()) stack.remove(stack.size() - 1);
        currentDepth--;
    }

    public int parse(StringBuilder sourcecode, int first)
    {
        int last = first, current = first;

        for (char currentChar; running && last < sourcecode.length()
                && (currentChar = sourcecode.charAt(last++)) != '}';)
        {
            if (currentChar == '{')
            {
                int braceOpen = last - 1; // position of {
                current = parse(sourcecode, last);
                int braceClose = current - 1; // position of }
                String pat = nospc(sourcecode.substring(first, braceOpen));
                String prod = sourcecode.substring(last, braceClose);

                productions.put(pat, prod);

                productionLocations.put(pat, new DefLocation(first, braceOpen, braceClose, prod));
                if (listener != null)
                {
                    listener.onDefinition(sourcecode.toString(), pat, prod,
                            first, braceOpen, braceClose, getStack());
                }
                last = first = current;
            }
            if (!running) break;
            if (listener != null)
            {
                listener.onScan(sourcecode.toString(), current, last, getStack());
            }

            String token = sourcecode.substring(current, last);
            String key = nospc(token);
            String production = productions.get(key);

            if (production != null)
            {
                DefLocation defLoc = productionLocations.get(key);

                if (listener != null && shouldPause(current, last))
                {
                    listener.onBeforeSubstitute(
                        sourcecode.toString(), token, production,
                        current, last, defLoc, getStack());
                    waitForStep();
                }

                StringBuilder value = new StringBuilder(production);
                pushFrame(token, production);
                parse(value, 0);
                popFrame();

                String resolved = value.toString();
                int oldLen = last - current;
                sourcecode.replace(current, last, resolved);

                // Adjust breakpoint offsets for this mutation
                adjustBreakpoints(current, oldLen, resolved.length());

                last = current += resolved.length();

                if (listener != null && shouldPause(current - resolved.length(), current))
                {
                    listener.onAfterSubstitute(
                        sourcecode.toString(),
                        current - resolved.length(), current,
                        getStack());
                    waitForStep();
                }
            }
        }

        return last;
    }

    private static String nospc(final String token) {
        return token.replace("\n", " ").replace("\r", " ").replace(" ", "");
    }

    public void parseFromStart(StringBuilder sourcecode)
    {
        running = true;
        currentDepth = 0;
        stack.clear();
        frameIdCounter = 0;
        productionLocations.clear();
        stack.add(new StackFrame(0, "", "(root)", frameIdCounter++));
        parse(sourcecode, 0);
        if (listener != null && running)
        {
            listener.onFinished(sourcecode.toString());
        }
    }

}