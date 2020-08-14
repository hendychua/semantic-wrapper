package io.github.hendychua.semantic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.util.Optional;

@FreeBuilder
@JsonDeserialize(builder = Symbol.Builder.class)
public interface Symbol {

    @FreeBuilder
    @JsonDeserialize(builder = SymbolSpan.Builder.class)
    interface SymbolSpan {
        Span start();
        Span end();

        class Builder extends Symbol_SymbolSpan_Builder {
        }
    }

    @FreeBuilder
    @JsonDeserialize(builder = ByteRange.Builder.class)
    interface ByteRange {
        int start();
        int end();

        class Builder extends Symbol_ByteRange_Builder {
        }
    }

    String symbol();
    String kind();
    String line();
    SymbolSpan span();
    String nodeType();
    Optional<String> syntaxType();
    SymbolSpan utf16CodeUnitSpan();
    ByteRange byteRange();

    class Builder extends Symbol_Builder {
    }
}
