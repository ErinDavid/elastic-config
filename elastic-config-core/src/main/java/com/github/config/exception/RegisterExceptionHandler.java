
package com.github.config.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

/**
 * 抛出RegisterException的异常处理类.
 * 
 * @author ZhangWei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegisterExceptionHandler {

    /**
     * 处理掉中断和连接失效异常并继续抛出RegisterException.
     * 
     * @param cause 待处理的异常.
     */
    public static void handleException(final Exception cause) {
        if (isIgnoredException(cause) || isIgnoredException(cause.getCause())) {
            log.debug("Elastic config: ignored exception for: {}", cause.getMessage());
        }
        else if (cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        else {
            throw new RegisterException(cause);
        }
    }

    private static boolean isIgnoredException(final Throwable cause) {
        return null != cause
            && (cause instanceof ConnectionLossException || cause instanceof NoNodeException || cause instanceof NodeExistsException);
    }
}
