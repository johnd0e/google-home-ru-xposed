package local.googlehomeru;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/** Rewrites one string field in a protobuf-lite message without depending on obfuscated fields. */
final class ProtoLanguageRewriter {
    private static final int LANGUAGE_FIELD_NUMBER = 3;

    private ProtoLanguageRewriter() {
    }

    static RewriteResult rewrite(Object message, String expectedLanguage, String targetLanguage)
            throws ReflectiveOperationException, InvalidWireDataException {
        if (message == null) {
            throw new IllegalArgumentException("protobuf message is null");
        }

        Method toByteArray = message.getClass().getMethod("toByteArray");
        byte[] serialized = (byte[]) toByteArray.invoke(message);
        FieldRewrite fieldRewrite = rewriteStringField(
                serialized, LANGUAGE_FIELD_NUMBER, expectedLanguage, targetLanguage);
        if (!fieldRewrite.changed) {
            return new RewriteResult(fieldRewrite.originalValue, fieldRewrite.originalValue,
                    message, false);
        }

        Object parser = message.getClass().getMethod("getParserForType").invoke(message);
        Method parseFrom = parser.getClass().getMethod("parseFrom", byte[].class);
        Object rewrittenMessage;
        try {
            rewrittenMessage = parseFrom.invoke(parser, (Object) fieldRewrite.bytes);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ReflectiveOperationException) {
                throw (ReflectiveOperationException) cause;
            }
            throw new InvalidWireDataException("protobuf parser rejected rewritten message", cause);
        }
        if (!message.getClass().isInstance(rewrittenMessage)) {
            throw new InvalidWireDataException("protobuf parser returned an unexpected message type");
        }
        return new RewriteResult(fieldRewrite.originalValue, targetLanguage,
                rewrittenMessage, true);
    }

    private static FieldRewrite rewriteStringField(
            byte[] input, int targetFieldNumber, String expectedValue, String replacement)
            throws InvalidWireDataException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length + replacement.length());
        int cursor = 0;
        int languageFields = 0;
        String originalValue = null;
        boolean changed = false;

        while (cursor < input.length) {
            int fieldStart = cursor;
            Varint tag = readVarint(input, cursor);
            cursor = tag.nextOffset;
            int fieldNumber = (int) (tag.value >>> 3);
            int wireType = (int) (tag.value & 7);
            if (fieldNumber == 0) {
                throw new InvalidWireDataException("protobuf field number is zero");
            }

            int valueStart = cursor;
            int valueEnd = skipValue(input, cursor, fieldNumber, wireType);
            if (fieldNumber == targetFieldNumber) {
                languageFields++;
                if (languageFields != 1 || wireType != 2) {
                    throw new InvalidWireDataException("language field is repeated or not a string");
                }
                Varint length = readVarint(input, cursor);
                int dataStart = length.nextOffset;
                int dataLength = checkedLength(length.value, input.length - dataStart);
                originalValue = new String(input, dataStart, dataLength, StandardCharsets.UTF_8);
                if (expectedValue.equals(originalValue)) {
                    output.write(input, fieldStart, tag.nextOffset - fieldStart);
                    byte[] replacementBytes = replacement.getBytes(StandardCharsets.UTF_8);
                    writeVarint(output, replacementBytes.length);
                    output.write(replacementBytes, 0, replacementBytes.length);
                    changed = true;
                } else {
                    output.write(input, fieldStart, valueEnd - fieldStart);
                }
            } else {
                output.write(input, fieldStart, valueEnd - fieldStart);
            }
            cursor = valueEnd;

            // Keep the bounds check explicit: a malformed field must fail closed.
            if (cursor < valueStart || cursor > input.length) {
                throw new InvalidWireDataException("protobuf field exceeds message bounds");
            }
        }

        if (languageFields == 0) {
            return new FieldRewrite(input, null, false);
        }
        return new FieldRewrite(changed ? output.toByteArray() : input, originalValue, changed);
    }

    private static int skipValue(byte[] input, int offset, int fieldNumber, int wireType)
            throws InvalidWireDataException {
        switch (wireType) {
            case 0:
                return readVarint(input, offset).nextOffset;
            case 1:
                return checkedEnd(offset, 8, input.length);
            case 2:
                Varint length = readVarint(input, offset);
                return checkedEnd(length.nextOffset, checkedLength(length.value,
                        input.length - length.nextOffset), input.length);
            case 3:
                int cursor = offset;
                while (cursor < input.length) {
                    Varint nestedTag = readVarint(input, cursor);
                    cursor = nestedTag.nextOffset;
                    int nestedFieldNumber = (int) (nestedTag.value >>> 3);
                    int nestedWireType = (int) (nestedTag.value & 7);
                    if (nestedWireType == 4) {
                        if (nestedFieldNumber != fieldNumber) {
                            throw new InvalidWireDataException("mismatched protobuf end group");
                        }
                        return cursor;
                    }
                    cursor = skipValue(input, cursor, nestedFieldNumber, nestedWireType);
                }
                throw new InvalidWireDataException("unterminated protobuf group");
            case 4:
                throw new InvalidWireDataException("unexpected protobuf end group");
            case 5:
                return checkedEnd(offset, 4, input.length);
            default:
                throw new InvalidWireDataException("unsupported protobuf wire type " + wireType);
        }
    }

    private static Varint readVarint(byte[] input, int offset) throws InvalidWireDataException {
        long value = 0;
        int shift = 0;
        int cursor = offset;
        while (cursor < input.length && shift < 64) {
            int next = input[cursor++] & 0xff;
            value |= (long) (next & 0x7f) << shift;
            if ((next & 0x80) == 0) {
                return new Varint(value, cursor);
            }
            shift += 7;
        }
        throw new InvalidWireDataException("malformed protobuf varint");
    }

    private static int checkedLength(long length, int remaining) throws InvalidWireDataException {
        if (length < 0 || length > remaining) {
            throw new InvalidWireDataException("protobuf length exceeds message bounds");
        }
        return (int) length;
    }

    private static int checkedEnd(int offset, int count, int limit) throws InvalidWireDataException {
        if (offset < 0 || count < 0 || offset > limit - count) {
            throw new InvalidWireDataException("protobuf field exceeds message bounds");
        }
        return offset + count;
    }

    private static void writeVarint(ByteArrayOutputStream output, long value) {
        while ((value & ~0x7fL) != 0) {
            output.write((int) ((value & 0x7f) | 0x80));
            value >>>= 7;
        }
        output.write((int) value);
    }

    static final class RewriteResult {
        final String originalLanguage;
        final String resultingLanguage;
        final Object message;
        final boolean rewritten;

        RewriteResult(String originalLanguage, String resultingLanguage,
                      Object message, boolean rewritten) {
            this.originalLanguage = originalLanguage;
            this.resultingLanguage = resultingLanguage;
            this.message = message;
            this.rewritten = rewritten;
        }
    }

    private static final class FieldRewrite {
        final byte[] bytes;
        final String originalValue;
        final boolean changed;

        FieldRewrite(byte[] bytes, String originalValue, boolean changed) {
            this.bytes = bytes;
            this.originalValue = originalValue;
            this.changed = changed;
        }
    }

    private static final class Varint {
        final long value;
        final int nextOffset;

        Varint(long value, int nextOffset) {
            this.value = value;
            this.nextOffset = nextOffset;
        }
    }

    static final class InvalidWireDataException extends Exception {
        InvalidWireDataException(String message) {
            super(message);
        }

        InvalidWireDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
