package io.github.hendychua.semantic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.util.List;

/**
 * Output from semantic parse [file] --json-symbols
 */
@FreeBuilder
@JsonDeserialize(builder = JsonSymbols.Builder.class)
public interface JsonSymbols {

    List<SemFile> files();

    class Builder extends JsonSymbols_Builder {
    }
}
