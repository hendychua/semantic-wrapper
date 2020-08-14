package io.github.hendychua.semantic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.util.List;

@FreeBuilder
@JsonDeserialize(builder = SemFile.Builder.class)
public interface SemFile {

    String path();
    String language();
    List<Symbol> symbols();

    class Builder extends SemFile_Builder {
    }
}
