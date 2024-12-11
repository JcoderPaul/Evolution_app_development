package me.oldboy.servlets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.IOException;
import java.io.OutputStream;

public class MockServletOutputStream extends ServletOutputStream {

    private OutputStream stream;

    public MockServletOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public String toString() {
        return stream.toString();
    }
}