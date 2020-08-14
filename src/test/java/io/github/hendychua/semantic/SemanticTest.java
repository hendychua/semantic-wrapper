package io.github.hendychua.semantic;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.AssertJUnit.assertEquals;

public class SemanticTest {

    private final Path binStub = Paths.get("binStub");
    private final Semantic semantic = spy(new Semantic(binStub));

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
}
