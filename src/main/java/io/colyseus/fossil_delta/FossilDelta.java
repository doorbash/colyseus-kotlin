package io.colyseus.fossil_delta;

import java.io.ByteArrayOutputStream;

public class FossilDelta {

    private static int[] zValue = new int[]{
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, 36,
            -1, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, -1, -1, -1, 63, -1
    };

    private static final int[] zDigits = new int[]{
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
            90, 95, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122,
            126
    };

    private static int checksum(byte[] arr) {
        int sum0 = 0, sum1 = 0, sum2 = 0, sum3 = 0;
        int z = 0, N = arr.length;
        //TODO measure if this unrolling is helpful.
        while (N >= 16) {
            sum0 += (arr[z] & 0xFF) + (arr[z + 4] & 0xFF) + (arr[z + 8] & 0xFF) + (arr[z + 12] & 0xFF);
            sum1 += (arr[z + 1] & 0xFF) + (arr[z + 5] & 0xFF) + (arr[z + 9] & 0xFF) + (arr[z + 13] & 0xFF);
            sum2 += (arr[z + 2] & 0xFF) + (arr[z + 6] & 0xFF) + (arr[z + 10] & 0xFF) + (arr[z + 14] & 0xFF);
            sum3 += (arr[z + 3] & 0xFF) + (arr[z + 7] & 0xFF) + (arr[z + 11] & 0xFF) + (arr[z + 15] & 0xFF);
            z += 16;
            N -= 16;
        }
        while (N >= 4) {
            sum0 += (arr[z] & 0xFF);
            sum1 += (arr[z + 1] & 0xFF);
            sum2 += (arr[z + 2] & 0xFF);
            sum3 += (arr[z + 3] & 0xFF);
            z += 4;
            N -= 4;
        }
        sum3 = (((sum3 + (sum2 << 8)) + (sum1 << 16)) + (sum0 << 24));
        /* jshint -W086 */
        switch (N) {
            case 3:
                sum3 += (arr[z + 2] & 0xFF) << 8; /* falls through */
            case 2:
                sum3 += (arr[z + 1] & 0xFF) << 16; /* falls through */
            case 1:
                sum3 += (arr[z] & 0xFF) << 24; /* falls through */
        }
        return sum3;
    }


    static class Reader {
        private byte[] a;
        private int pos;

        Reader(byte[] array) {
            this.a = array;
            this.pos = 0;
        }

        boolean haveBytes() {
            return this.pos < this.a.length;
        }

        byte getByte() throws Exception {
            byte b = this.a[this.pos];
            this.pos++;
            if (this.pos > this.a.length) throw new Exception("out of bounds");
            return b;
        }

        char getChar() throws Exception {
            return (char) this.getByte();
        }

        int getInt() throws Exception {
            int v = 0, c;
            while (this.haveBytes() && (c = zValue[0x7f & this.getByte()]) >= 0) {
                v = (v << 6) + c;
            }
            this.pos--;
            return v;
        }
    }

    static class CustomByteArrayOutputStream extends ByteArrayOutputStream {
        byte[] getBuf(){
            return this.buf;
        }
    }

    static class Writer {

        CustomByteArrayOutputStream a;

        Writer() {
            this.a = new CustomByteArrayOutputStream();
        }

        byte[] toArray() {
            return a.toByteArray();
        }

        void putByte(byte b) {
            this.a.write(b);
        }

        void putChar(char s) {
            this.putByte((byte) s);
        }

        public void putInt(int v) {
            int i, j;
            CustomByteArrayOutputStream zBuf = new CustomByteArrayOutputStream();
            if (v == 0) {
                this.putChar('0');
                return;
            }
            for (i = 0; v > 0; i++, v >>>= 6)
                zBuf.write(zDigits[v & 0x3f]);
            for (j = i - 1; j >= 0; j--)
                this.putByte(zBuf.getBuf()[j]);
        }

        void putArray(byte[] a, int start, int end) {
            this.a.write(a, start, end - start);
        }
    }

    public static byte[] apply(byte[] src, byte[] delta) throws Exception {
        int limit, total = 0;
        Reader zDelta = new Reader(delta);
        int lenSrc = src.length;
        int lenDelta = delta.length;

        limit = zDelta.getInt();
        if (zDelta.getChar() != '\n')
            throw new Exception("size integer not terminated by \'\\n\'");
        Writer zOut = new Writer();
        while (zDelta.haveBytes()) {
            int cnt, ofst;
            cnt = zDelta.getInt();

            switch (zDelta.getChar()) {
                case '@':
                    ofst = zDelta.getInt();
                    if (zDelta.haveBytes() && zDelta.getChar() != ',')
                        throw new Exception("copy command not terminated by \',\'");
                    total += cnt;
                    if (total > limit)
                        throw new Exception("copy exceeds output file size");
                    if (ofst + cnt > lenSrc)
                        throw new Exception("copy extends past end of input");
                    zOut.putArray(src, ofst, ofst + cnt);
                    break;

                case ':':
                    total += cnt;
                    if (total > limit)
                        throw new Exception("insert command gives an output larger than predicted");
                    if (cnt > lenDelta)
                        throw new Exception("insert count exceeds size of delta");
                    zOut.putArray(zDelta.a, zDelta.pos, zDelta.pos + cnt);
                    zDelta.pos += cnt;
                    break;

                case ';':
                    byte[] out = zOut.toArray();
                    if (cnt != checksum(out))
                        throw new Exception("bad checksum");
                    if (total != limit)
                        throw new Exception("generated size does not match predicted size");
                    return out;

                default:
                    throw new Exception("unknown delta operator");
            }
        }
        throw new Exception("unterminated delta");
    }
}
