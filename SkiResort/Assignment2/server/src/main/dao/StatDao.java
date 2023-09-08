package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class StatDao {

  private static BasicDataSource dataSource;
//  private Connection conn;


  public StatDao() {
    dataSource = DBCP.getDataSource();
//    try {
//      conn = dataSource.getConnection();
//    } catch (SQLException e) {
//      e.printStackTrace();
//    }
  }

  public void insertInfo(long startTime, String method, long latency, Integer statusCode) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement =
        "INSERT INTO Stats(startTimeStamp, method, latency, statusCode) "
            +
            "VALUES (?,?,?,?)";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn
          .prepareStatement(insertQueryStatement);
      preparedStatement.setLong(1, startTime);
      preparedStatement.setString(2, method);
      preparedStatement.setLong(3, latency);
      preparedStatement.setInt(4, statusCode);

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }

  public Long getMean() {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ResultSet results = null;
    Long res = -1L;

    String inStmt = "SELECT AVG(latency) AS mean From Stats";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn
          .prepareStatement(inStmt);
      results = preparedStatement.executeQuery();
      if (results.next()) {
        res = results.getLong("mean");
        return res;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return res;
  }

  public Long getMax() {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ResultSet results = null;
    Long res = -1L;

    String inStmt = "SELECT MAX(latency) AS maxRes FROM Stats";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(inStmt);
      results = preparedStatement.executeQuery();
      if (results.next()) {
        res = results.getLong("maxRes");
        return res;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return res;
  }
}