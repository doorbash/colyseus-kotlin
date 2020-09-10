package io.colyseus.serializer.schema;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Decoder {

    public static Object decodePrimitiveType(String type, byte[] bytes, Iterator it) {
        switch (type) {
            case "string":
                return decodeString(bytes, it);
            case "number":
                return decodeNumber(bytes, it);
            case "int8":
                return decodeInt8(bytes, it);
            case "uint8":
                return decodeUint8(bytes, it);
            case "int16":
                return decodeInt16(bytes, it);
            case "uint16":
                return decodeUint16(bytes, it);
            case "int32":
                return decodeInt32(bytes, it);
            case "uint32":
                return decodeUint32(bytes, it);
            case "int64":
                return decodeInt64(bytes, it);
            case "uint64":
                return decodeUint64(bytes, it);
            case "float32":
                return decodeFloat32(bytes, it);
            case "float64":
                return decodeFloat64(bytes, it);
            case "boolean":
                return decodeBoolean(bytes, it);
        }
        return null;
    }

    public static float decodeNumber(byte[] bytes, Iterator it) {
        int prefix = bytes[it.offset++] & 0xFF;

        if (prefix < 128) {
            // positive fixint
            return prefix;

        } else if (prefix == 0xca) {
            // float 32
            return decodeFloat32(bytes, it);

        } else if (prefix == 0xcb) {
            // float 64
            return (float) decodeFloat64(bytes, it);

        } else if (prefix == 0xcc) {
            // uint 8
            return decodeUint8(bytes, it);

        } else if (prefix == 0xcd) {
            // uint 16
            return decodeUint16(bytes, it);

        } else if (prefix == 0xce) {
            // uint 32
            return decodeUint32(bytes, it);

        } else if (prefix == 0xcf) {
            // uint 64
            return decodeUint64(bytes, it);
        } else if (prefix == 0xd0) {
            // int 8
            return decodeInt8(bytes, it);

        } else if (prefix == 0xd1) {
            // int 16
            return decodeInt16(bytes, it);

        } else if (prefix == 0xd2) {
            // int 32
            return decodeInt32(bytes, it);

        } else if (prefix == 0xd3) {
            // int 64
            return decodeInt64(bytes, it);
        } else if (prefix > 0xdf) {
            // negative fixint
            return (0xff - prefix + 1) * -1;
        }

        return Float.NaN;
    }

    public static byte decodeInt8(byte[] bytes, Iterator it) {
        return bytes[it.offset++];
    }

    public static short decodeUint8(byte[] bytes, Iterator it) {
        return (short) (bytes[it.offset++] & 0xFF);
    }

    public static short decodeInt16(byte[] bytes, Iterator it) {
        short ret = ByteBuffer.wrap(bytes, it.offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        it.offset += 2;
        return ret;
    }

    public static int decodeUint16(byte[] bytes, Iterator it) {
        int ret = ByteBuffer.wrap(bytes, it.offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xffff;
        it.offset += 2;
        return ret;
    }

    public static int decodeInt32(byte[] bytes, Iterator it) {
        int ret = ByteBuffer.wrap(bytes, it.offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        it.offset += 4;
        return ret;
    }

    public static long decodeUint32(byte[] bytes, Iterator it) {
        long ret = ByteBuffer.wrap(bytes, it.offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffL;
        it.offset += 4;
        return ret;
    }

    public static float decodeFloat32(byte[] bytes, Iterator it) {
        float ret = ByteBuffer.wrap(bytes, it.offset, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        it.offset += 4;
        return ret;
    }

    public static double decodeFloat64(byte[] bytes, Iterator it) {
        double ret = ByteBuffer.wrap(bytes, it.offset, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        it.offset += 8;
        return ret;
    }

    public static long decodeInt64(byte[] bytes, Iterator it) {
        long ret = ByteBuffer.wrap(bytes, it.offset, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
        it.offset += 8;
        return ret;
    }

    public static long decodeUint64(byte[] bytes, Iterator it) {
        // There is no ulong type in Java so let's use long instead ¯\_(ツ)_/¯
        long ret = ByteBuffer.wrap(bytes, it.offset, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
        it.offset += 8;
        return ret;
    }

    public static boolean decodeBoolean(byte[] bytes, Iterator it) {
        return decodeUint8(bytes, it) > 0;
    }

    public static String decodeString(byte[] bytes, Iterator it) {
        int prefix = bytes[it.offset++] & 0xff;

        int length = 0;
        if (prefix < 0xc0) {
            // fixstr
            length = prefix & 0x1f;
        } else if (prefix == 0xd9) {
            length = (int) decodeUint8(bytes, it);
        } else if (prefix == 0xda) {
            length = decodeUint16(bytes, it);
        } else if (prefix == 0xdb) {
            length = (int) decodeUint32(bytes, it);
        }

        byte[] _bytes = new byte[length];
        System.arraycopy(bytes, it.offset, _bytes, 0, length);
        String str = new String(_bytes, StandardCharsets.UTF_8);
        it.offset += length;
        return str;
    }

    public static boolean nilCheck(byte[] bytes, Iterator it) {
        return bytes[it.offset] == Schema.SPEC.NIL;
    }

    public static boolean indexChangeCheck(byte[] bytes, Iterator it) {
        return bytes[it.offset] == Schema.SPEC.INDEX_CHANGE;
    }

    public static boolean numberCheck(byte[] bytes, Iterator it) {
        int prefix = bytes[it.offset] & 0xFF;
        return prefix < 0x80 || (prefix >= 0xca && prefix <= 0xd3);
    }

}
