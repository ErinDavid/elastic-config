package com.github.config.exception;

import java.io.IOException;

/**
 * 文件没有发现所抛出的异常.
 * 
 * @author ZhangWei
 */
public final class FileNotFoundException extends ElasticConfigException {

    private static final long serialVersionUID = 316825485808885546L;

    private static final String MSG = "CAN NOT found local properties files: [%s].";

    public FileNotFoundException(final String localPropertiesFileName) {
        super(MSG, localPropertiesFileName);
    }

    public FileNotFoundException(final IOException cause) {
        super(cause);
    }
}
