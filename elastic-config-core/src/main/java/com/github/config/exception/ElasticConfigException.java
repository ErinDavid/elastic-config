
package com.github.config.exception;

/**
 * 统一配置异常基类.
 * 
 * @author ZhangWei
 */
public class ElasticConfigException extends RuntimeException {

    private static final long serialVersionUID = -509739591923183173L;

    public ElasticConfigException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }

    public ElasticConfigException(final Throwable cause) {
        super(cause);
    }

}
