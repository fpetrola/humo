package ar.net.fpetrola.humo.interpreter;

/**
 * Intérprete Humo con Trie puro: lookup O(keyLen), sin backward scan ni for interno.
 */
public class TrieArrayInterpreter {

    public int parse(char[] in, int l) {
        int mN = l * 2 + 1;
        int[] tr = new int[mN * 128];
        int[] v = new int[mN];
        int[] dS = new int[Math.min(in.length, l * 6) * 3];
        int[] h = new int[in.length];
        int w = l, r = 0, d = 0, k = 0, n = 1, t = 0, b = 0, i, m, p, s;

        while (r!= l) {
            char c = in[w++] = in[r++];

            if (c == '{') {
                i = ++d * 3;
                dS[i] = 0;
                dS[i + 1] = w - 1;
                dS[i + 2] = b;
                k = t = b = 0;
            } else if (c == '}') {
                i = d-- * 3;
                p = dS[i];
                if (p!= 0 && (d == 0 || dS[d * 3]!= 0 || dS[d * 3] < p)) {
                    w--;
                    r = p;
                } else {
                    s = dS[i + 2];
                    if (s!= 0) v[s] = dS[i + 1] + 1;
                    b = 0;
                }
                k = t = 0;
            } else {
                k++;
                h[w - 1] = b;
                i = b * 128 + (c & 0x7F);
                if (tr[i] == 0) tr[i] = n++;
                b = tr[i];

                if (t >= 0) {
                    m = tr[t * 128 + (c & 0x7F)];
                    if (m!= 0) {
                        t = m;
                        if (v[t]!= 0) {
                            i = ++d * 3;
                            dS[i] = r;
                            r = v[t];
                            w-= k;
                            b = h[w];
                            k = t = 0;
                        }
                    } else {
                        t = -1;
                    }
                }
            }
        }
        return w;
    }
}