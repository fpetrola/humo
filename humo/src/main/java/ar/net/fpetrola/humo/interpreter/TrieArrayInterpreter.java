package ar.net.fpetrola.humo.interpreter;

/**
 * Intérprete Humo con Trie puro: lookup O(keyLen), sin backward scan ni for interno.
 */
public class TrieArrayInterpreter {

    private static boolean isWS(char c) { return c == ' ' || c == '\n' || c == '\r' || c == '\t'; }

    public int parse(char[] buffer, int inputLength) {
        int maxNodes = inputLength * 2 + 1;
        int[] trie = new int[maxNodes * 128];
        int[] values = new int[maxNodes];
        int[] depthStack = new int[Math.min(buffer.length, inputLength * 6) * 3];
        int[] buildHistory = new int[buffer.length];
        int writePos = inputLength, readPos = 0, depth = 0, keyLength = 0, nodeCount = 1;
        int trieNode = 0, buildNode = 0;

        while (readPos!= inputLength) {
            char c = buffer[writePos++] = buffer[readPos++];

            if (c == '{') {
                int stackIndex = ++depth * 3;
                depthStack[stackIndex] = 0;
                depthStack[stackIndex + 1] = writePos - 1;
                depthStack[stackIndex + 2] = buildNode;
                keyLength = 0; trieNode = 0; buildNode = 0;
            } else if (c == '}') {
                int stackIndex = depth-- * 3;
                int returnPos = depthStack[stackIndex];
                if (returnPos!= 0 && (depth == 0 || depthStack[depth * 3]!= 0 || depthStack[depth * 3] < returnPos)) {
                    writePos--;
                    readPos = returnPos;
                } else {
                    int savedBuildNode = depthStack[stackIndex + 2];
                    if (savedBuildNode!= 0) values[savedBuildNode] = depthStack[stackIndex + 1] + 1;
                    buildNode = 0;
                }
                keyLength = 0; trieNode = 0;
            } else {
                keyLength++;
                buildHistory[writePos - 1] = buildNode;
                int trieIndex = buildNode * 128 + (c & 0x7F);
                if (trie[trieIndex] == 0) trie[trieIndex] = nodeCount++;
                buildNode = trie[trieIndex];

                if (trieNode >= 0) {
                    int nextNode = trie[trieNode * 128 + (c & 0x7F)];
                    if (nextNode!= 0) {
                        trieNode = nextNode;
                        if (values[trieNode]!= 0) {
                            int stackIndex = ++depth * 3;
                            depthStack[stackIndex] = readPos;
                            readPos = values[trieNode];
                            writePos-= keyLength;
                            buildNode = buildHistory[writePos];
                            keyLength = 0; trieNode = 0;
                        }
                    } else {
                        trieNode = -1; // miss: no hay prefijo válido, esperar reset estructural
                    }
                }
            }
        }
        return writePos;
    }
}