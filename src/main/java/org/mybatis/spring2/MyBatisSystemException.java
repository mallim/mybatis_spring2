package org.mybatis.spring2;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Created by User on 18/3/2015.
 */
public class MyBatisSystemException extends UncategorizedDataAccessException {

    private static final long serialVersionUID = -5284728621670758939L;

    public MyBatisSystemException(Throwable cause) {
        super(null, cause);
    }
}
