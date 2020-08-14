package io.github.hendychua.semantic;

import org.inferred.freebuilder.FreeBuilder;

import java.nio.file.Path;

@FreeBuilder
public interface FailedFile {
    Path filePath();
    String errorMsg();
    class Builder extends FailedFile_Builder {
    }
}
