package org.mybatis.spring2;

import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.util.Assert.notNull;

/**
 * Created by User on 18/3/2015.
 */
public class SpringManagedTransaction implements Transaction {

    private static final Log LOGGER = LogFactory.getLog(SpringManagedTransaction.class);

    private final DataSource dataSource;

    private Connection connection;

    private boolean isConnectionTransactional;

    private boolean autoCommit;

    public SpringManagedTransaction(DataSource dataSource) {
        notNull(dataSource, "No DataSource specified");
        this.dataSource = dataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (this.connection == null) {
            openConnection();
        }
        return this.connection;
    }

    /**
     * Gets a connection from Spring transaction manager and discovers if this
     * {@code Transaction} should manage connection or let it to Spring.
     * <p>
     * It also reads autocommit setting because when using Spring Transaction MyBatis
     * thinks that autocommit is always false and will always call commit/rollback
     * so we need to no-op that calls.
     */
    private void openConnection() throws SQLException {
        this.connection = DataSourceUtils.getConnection(this.dataSource);
        this.autoCommit = this.connection.getAutoCommit();
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, this.dataSource);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "JDBC Connection ["
                            + this.connection
                            + "] will"
                            + (this.isConnectionTransactional ? " " : " not ")
                            + "be managed by Spring");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Committing JDBC Connection [" + this.connection + "]");
            }
            this.connection.commit();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Rolling back JDBC Connection [" + this.connection + "]");
            }
            this.connection.rollback();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        DataSourceUtils.releaseConnection(this.connection, this.dataSource);
    }

}
