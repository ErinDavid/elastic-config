package com.github.config.exception;

import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.junit.Test;

public class RegisterExceptionHandlerTest {

    @Test
    public void assertHandleExceptionWithInterruptedException() {

        RegisterExceptionHandler.handleException(new InterruptedException());
    }

    @Test(expected = RegisterException.class)
    public void assertHandleExceptionWithOtherException() {
        RegisterExceptionHandler.handleException(new RuntimeException());
    }

    @Test
    public void assertHandleExceptionWithConnectionLossException() {
        RegisterExceptionHandler.handleException(new ConnectionLossException());
    }

}
