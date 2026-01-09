package org.example.node;
import java.time.LocalDateTime;

public sealed interface ValueType {
    record None() implements ValueType {}
    record Int64(long value) implements ValueType {}
    record Double(double value) implements ValueType {}
    record Bool(boolean value) implements ValueType {}
    record Str(String value) implements ValueType {}
    record DateTime(LocalDateTime value) implements ValueType {}
    record Binary(byte[] value) implements ValueType {}
}
