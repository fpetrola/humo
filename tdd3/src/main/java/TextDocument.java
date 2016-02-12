public interface TextDocument
{
    int getCaretPosition();

    int getSelectionStart();

    int getSelectionEnd();

    void setCaretPosition(int caretPosition);

    void setSpan(String style, int start, int end);

    void clear();

    void insert(int start, String string);

    int getLength();

    void delete(int startPosition, int endPosition);

    void setAuto(boolean auto);

    boolean isAuto();

    String getText();
}