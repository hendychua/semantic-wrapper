package io.github.hendychua.semantic;

import org.inferred.freebuilder.FreeBuilder;

import java.util.List;

/**
 * Output after parsing a directory for --json-symbols.
 */
@FreeBuilder
public interface ParseDirectoryOutput {

    /**
     * JsonSymbols of files that passed.
     */
    List<JsonSymbols> jsonSymbols();

    /**
     * Files that failed parsing.
     */
    List<FailedFile> failures();

    class Builder extends ParseDirectoryOutput_Builder {
    }
}
