package com.github.config.exception;

/**
 * 注册异常
 * 
 * @author ZhangWei
 */
public class RegisterException extends ElasticConfigException {

    private static final long serialVersionUID = -5128005349996149136L;

    public RegisterException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }

    public RegisterException(final Exception cause) {
        super(cause);
    }
}
