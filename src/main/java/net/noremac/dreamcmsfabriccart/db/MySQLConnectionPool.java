package net.noremac.dreamcmsfabriccart.db;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MySQLConnectionPool implements java.io.Closeable {
    private final HikariDataSource ds;
    public final ScheduledExecutorService MYSQL_EXECUTOR = Executors.newScheduledThreadPool(1);
    public final ExecutorService ASYNC_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public MySQLConnectionPool(String host, String user, String password, String db) throws ClassNotFoundException {
        MysqlDataSource mysqlSource = new MysqlDataSource();


        try {
            mysqlSource.setCachePrepStmts(true);
            mysqlSource.setEnableQueryTimeouts(true);
            mysqlSource.setLoginTimeout(5);
            mysqlSource.setConnectTimeout(5000);
            mysqlSource.setAutoClosePStmtStreams(true);
            mysqlSource.setPrepStmtCacheSize(512);
            mysqlSource.setPrepStmtCacheSqlLimit(2048);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        String[] hostdata = host.split(":");
        if (hostdata.length == 2) {
            mysqlSource.setServerName(hostdata[0]);
            mysqlSource.setPortNumber(Integer.parseInt(hostdata[1]));
        } else {
            mysqlSource.setServerName(host);
        }
        mysqlSource.setUser(user);
        mysqlSource.setPassword(password);
        mysqlSource.setDatabaseName(db);

//        Class.forName("com.zaxxer.hikari.HikariDataSource");
        this.ds = new HikariDataSource();
        this.ds.setDataSource(mysqlSource);

        this.ds.setMaximumPoolSize(25);
        this.ds.setValidationTimeout(5000L);
        this.ds.setMinimumIdle(2);
        this.ds.setPoolName("Core-Connection-Pool");

        this.ds.addDataSourceProperty("useUnicode", "true");
        this.ds.addDataSourceProperty("characterEncoding", "utf-8");
        this.ds.addDataSourceProperty("rewriteBatchedStatements", "true");
        this.ds.setLeakDetectionThreshold(1000);
        this.ds.addDataSourceProperty("cachePrepStmts", "true");
        this.ds.addDataSourceProperty("prepStmtCacheSize", "512");
        this.ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    }

    public void close() {
        try {
            this.ds.close();
        } catch (Exception localException) {
        }
    }

    public Connection getConnection() {
        try {
            return this.ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.sql.ResultSet selectQuery(String query, Object... args) {
        PreparedStatement stmt;
        Connection connection = this.getConnection();
        ResultSet resultSet;

        try {
            stmt = getStatement(connection, query, args);
            resultSet = stmt.executeQuery();
            return resultSet;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MYSQL_EXECUTOR.schedule(() -> {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, 250, TimeUnit.MILLISECONDS);
        }
        return null;
    }

    public java.sql.ResultSet safeSelectQuery(Connection connection, String query, Object... args) {
        PreparedStatement stmt = null;
        try {
            stmt = getStatement(connection, query, args);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int updateQuery(String query, Object... args) {
        Connection con = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = getStatement(con, query, args);
            return stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public boolean query(String query, Object... args) {
        Connection con = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = getStatement(con, query, args);
            stmt.execute();
            stmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private PreparedStatement getStatement(Connection connection, String query, Object... args) {
        int i = 1;

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            if (args != null && args.length > 0) {
                Object[] var5 = args;
                int var6 = args.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    Object arg = var5[var7];
                    if (arg instanceof String) {
                        stmt.setString(i, (String) arg);
                    } else if (arg instanceof Integer) {
                        stmt.setInt(i, (Integer) arg);
                    } else if (arg instanceof Long) {
                        stmt.setLong(i, (Long) arg);
                    } else if (arg instanceof Double) {
                        stmt.setDouble(i, (Double) arg);
                    } else if (arg instanceof Float) {
                        stmt.setFloat(i, (Float) arg);
                    } else if (arg instanceof byte[]) {
                        stmt.setBytes(i, (byte[]) arg);
                    } else if (arg instanceof UUID) {
                        stmt.setString(i, arg.toString());
                    } else {
                        stmt.setObject(i, arg);
                    }

                    ++i;
                }
            }

            return stmt;
        } catch (SQLException var9) {
            var9.printStackTrace();
            return null;
        }
    }

}
