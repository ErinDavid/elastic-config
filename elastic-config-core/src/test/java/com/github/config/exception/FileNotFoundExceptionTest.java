package com.github.config.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class FileNotFoundExceptionTest {

    @Test
    public void assertGetMessage() {
        assertThat(new FileNotFoundException("/invalid/invalid_file.properties").getMessage(),
            is("CAN NOT found local properties files: [/invalid/invalid_file.properties]."));
    }

    @Test
    public void assertGetMessageForCause() {
        assertThat(new FileNotFoundException(new IOException("io exception")).getMessage(),
            is("java.io.IOException: io exception"));
    }
}
