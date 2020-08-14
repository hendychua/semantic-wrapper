package io.github.hendychua.semantic;

public class NoLanguageForBlobException extends Exception {
    private static final long serialVersionUID = 6971636749249665246L;

    public NoLanguageForBlobException(String message) {
        super(message);
    }
}
