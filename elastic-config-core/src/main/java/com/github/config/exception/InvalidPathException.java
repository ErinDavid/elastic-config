package com.github.config.exception;

/**
 * 无效文件路径异常
 * 
 * @author ZhangWei
 */
public class InvalidPathException extends ElasticConfigException {

    private static final long serialVersionUID = -900845428830099003L;

    public InvalidPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPathException(String message) {
        super(message);
    }

    public InvalidPathException(Throwable cause) {
        super(cause);
    }

}
