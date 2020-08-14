package io.github.hendychua.semantic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.util.Optional;

@FreeBuilder
@JsonDeserialize(builder = Span.Builder.class)
public interface Span {

    Optional<Integer> line();
    Optional<Integer> column();

    class Builder extends Span_Builder {
    }
}
