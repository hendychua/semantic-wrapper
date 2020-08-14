package io.github.hendychua.semantic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A wrapper for Github's semantic library (https://github.com/github/semantic).
 */
public class Semantic {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.registerModule(new Jdk8Module());
    }

    private final Path binPath;

    /**
     * Creates a new Semantic instance with the provided semantic binary path.
     * @param binPath path to semantic binary.
     */
    public Semantic(Path binPath) {
        this.binPath = binPath;
    }

    /**
     * Parses filePath and returns the Json symbols output.
     * @param filePath path to the source file to parse.
     * @return a {@link JsonSymbols} instance.
     * @throws IOException when the process throws an IOException or when semantic returns a non-zero
     * code and the error is not due to "NoLanguageForBlob".
     * @throws InterruptedException when the process throws an InterruptedException
     * @throws NoLanguageForBlobException when semantic does not support the file's language.
     */
    public JsonSymbols parseJsonSymbols(Path filePath) throws IOException, InterruptedException,
            NoLanguageForBlobException {
        return parse(filePath, "--json-symbols", JsonSymbols.class);
    }

    /**
     * Searches directory recursively for files and parse them for Json symbols. If ext is not null,
     * parses only files that end with the extension.
     * @param dir the directory to search through.
     * @param ext (optional) file extension e.g. ".java", ".py"
     * @param failFast whether to throw an exception immediately if a parse operation fails.
     *                 If false, the operation will continue even if some files fail to parse, producing
     *                 {@link ParseDirectoryOutput} instance at the end which will consist of the list of
     *                 failed files.
     * @return a {@link ParseDirectoryOutput} instance.
     */
    public ParseDirectoryOutput parseJsonSymbolsInDir(Path dir, @Nullable String ext, boolean failFast) throws
            IOException, InterruptedException, NoLanguageForBlobException {
        final List<Path> matchedFiles;
        try (Stream<Path> pathStream = Files.walk(dir).filter(Files::isRegularFile)
                .filter(path -> ext == null || path.toString().endsWith(ext))) {
            matchedFiles = pathStream.collect(toList());
        }

        final List<JsonSymbols> jsonSymbols = new ArrayList<>();
        final List<FailedFile> failedFiles = new ArrayList<>();

        for (Path file: matchedFiles) {
            try {
                jsonSymbols.add(parseJsonSymbols(file));
            } catch (IOException | InterruptedException | NoLanguageForBlobException ex) {
                if (failFast) {
                    throw ex;
                }

                failedFiles.add(new FailedFile.Builder()
                        .filePath(file)
                        .errorMsg(ex.getMessage())
                        .build()
                );
            }
        }

        return new ParseDirectoryOutput.Builder()
                .addAllJsonSymbols(jsonSymbols)
                .addAllFailures(failedFiles)
                .build();
    }

    private <T> T parse(Path filePath, String option, Class<T> klass) throws IOException, InterruptedException,
            NoLanguageForBlobException {
        final Process process = execute(binPath.toString(), "parse", filePath.toString(), option);
        final OutputStream outputStream = process.getOutputStream();
        if (outputStream != null) {
            outputStream.close();
        }

        final String output;
        try (final InputStream inputStream = process.getInputStream()) {
            output = IOUtils.toString(inputStream, Charset.defaultCharset());
        }

        final int rc = process.waitFor();
        if (rc != 0) {
            if (output.startsWith("NoLanguageForBlob")) {
                throw new NoLanguageForBlobException(output);
            } else {
                throw new IOException(output);
            }
        }

        return MAPPER.readValue(output, klass);
    }

    @VisibleForTesting
    Process execute(String... command) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
}
