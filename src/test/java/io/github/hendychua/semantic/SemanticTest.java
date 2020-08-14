package io.github.hendychua.semantic;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;

public class SemanticTest {

    private final Path binStub = Paths.get("binStub");
    private final Semantic semantic = spy(new Semantic(binStub));

    @BeforeMethod
    public void before() {
        reset(semantic);
    }

    @Test
    public void testParseJsonSymbols() throws Exception {
        final Process mockProcess = mock(Process.class);
        doReturn(getClass().getResourceAsStream("foo.py.semantic.symbols")).when(mockProcess).getInputStream();
        doReturn(0).when(mockProcess).waitFor();

        final Path filePath = Paths.get("foo.py");
        doReturn(mockProcess).when(semantic).execute(eq(binStub.toString()), eq("parse"), eq(filePath.toString()), eq("--json-symbols"));
        final JsonSymbols jsonSymbols = semantic.parseJsonSymbols(filePath);
        assertEquals(1, jsonSymbols.files().size());
        final SemFile semFile = jsonSymbols.files().get(0);
        assertEquals("Python", semFile.language());
        assertEquals("foo.py", semFile.path());
        assertEquals(3, semFile.symbols().size());
    }

    @Test(expectedExceptions = NoLanguageForBlobException.class)
    public void testParseJsonSymbolsNotSupported() throws Exception {
        final Process mockProcess = mock(Process.class);
        doReturn(new ByteArrayInputStream("NoLanguageForBlob(foo.txt)".getBytes())).when(mockProcess).getInputStream();
        doReturn(1).when(mockProcess).waitFor();

        final Path filePath = Paths.get("foo.py");
        doReturn(mockProcess).when(semantic).execute(eq(binStub.toString()), eq("parse"), eq(filePath.toString()), eq("--json-symbols"));
        final JsonSymbols jsonSymbols = semantic.parseJsonSymbols(filePath);
    }

    @Test
    public void testParseJsonSymbolsInDir() throws Exception {
        final Path dir = Paths.get(getClass().getResource("testdir").getPath());
        final Path fooPy = dir.resolve("foo.py");
        final Path fooJava = dir.resolve("subdir").resolve("Foo.java");
        final Path testTxt = dir.resolve("subdir").resolve("test.txt");

        final JsonSymbols mockSymbols = mock(JsonSymbols.class);
        doReturn(mockSymbols).when(semantic).parseJsonSymbols(eq(fooPy));
        doReturn(mockSymbols).when(semantic).parseJsonSymbols(eq(fooJava));
        doThrow(new NoLanguageForBlobException("no language")).when(semantic).parseJsonSymbols(eq(testTxt));

        ParseDirectoryOutput output = semantic.parseJsonSymbolsInDir(dir, null, false);

        assertEquals(2, output.jsonSymbols().size());
        assertEquals(Set.of(testTxt), output.failures().stream().map(FailedFile::filePath).collect(toSet()));
    }

    @Test
    public void testParseJsonSymbolsInDirJavaOnly() throws Exception {
        final Path dir = Paths.get(getClass().getResource("testdir").getPath());
        final Path fooJava = dir.resolve("subdir").resolve("Foo.java");

        final JsonSymbols mockSymbols = mock(JsonSymbols.class);
        doReturn(mockSymbols).when(semantic).parseJsonSymbols(eq(fooJava));

        ParseDirectoryOutput output = semantic.parseJsonSymbolsInDir(dir, ".java", false);

        verify(semantic, times(1)).parseJsonSymbols(any());

        assertEquals(1, output.jsonSymbols().size());
        assertEquals(0, output.failures().size());
    }

    @Test(expectedExceptions = NoLanguageForBlobException.class)
    public void testParseJsonSymbolsInDirFailFast() throws Exception {
        final Path dir = Paths.get(getClass().getResource("testdir").getPath());
        final Path fooPy = dir.resolve("foo.py");
        final Path fooJava = dir.resolve("subdir").resolve("Foo.java");
        final Path testTxt = dir.resolve("subdir").resolve("test.txt");

        final JsonSymbols mockSymbols = mock(JsonSymbols.class);
        doReturn(mockSymbols).when(semantic).parseJsonSymbols(eq(fooPy));
        doReturn(mockSymbols).when(semantic).parseJsonSymbols(eq(fooJava));
        doThrow(new NoLanguageForBlobException("no language")).when(semantic).parseJsonSymbols(eq(testTxt));

        ParseDirectoryOutput output = semantic.parseJsonSymbolsInDir(dir, null, true);
    }
}
