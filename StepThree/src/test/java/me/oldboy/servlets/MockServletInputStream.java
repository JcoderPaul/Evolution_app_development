package me.oldboy.servlets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;
import java.io.InputStream;

public class MockServletInputStream extends ServletInputStream {

    private final InputStream stream;
    private int lastIndexRetrieved = -1;
    private ReadListener readListener = null;

    public MockServletInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public boolean isFinished() {
        try {
            return stream.readAllBytes().length-1 == lastIndexRetrieved;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isReady() {
        return isFinished();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        this.readListener = readListener;
        if (!isFinished()) {
            try {
                readListener.onDataAvailable();
            } catch (IOException e) {
                readListener.onError(e);
            }
        } else {
            try {
                readListener.onAllDataRead();
            } catch (IOException e) {
                readListener.onError(e);
            }
        }
    }
}