package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.dbcp2.BasicDataSource;

public class SkierDao {

  private static BasicDataSource dataSource;


  public SkierDao() {
    dataSource = DBCP.getDataSource();
  }

  public void createLiftRide(Integer skierId, Integer resortId, Integer seasonId, Integer dayId,
      Integer liftTime, Integer liftId, Integer verticalRise,
      ConcurrentHashMap<String, Integer> cache) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement =
        "INSERT INTO LiftRides(skierId, resortId, seasonId, dayId, liftTime, liftId, verticalRise) "
            +
            "VALUES (?,?,?,?,?,?,?)";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn
          .prepareStatement(insertQueryStatement);
      preparedStatement.setInt(1, skierId);
      preparedStatement.setInt(2, resortId);
      preparedStatement.setInt(3, seasonId);
      preparedStatement.setInt(4, dayId);
      preparedStatement.setInt(5, liftTime);
      preparedStatement.setInt(6, liftId);
      preparedStatement.setInt(7, verticalRise);
      preparedStatement.executeUpdate();
      String key = convertString(dayId, skierId);
      if (cache.containsKey(key)) {
        cache.put(key, cache.get(key) + verticalRise);
      } else {
        cache.put(key, verticalRise);
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
  }

  public Integer getVerticalForSpecificDay(Integer dayId, Integer skierId, ConcurrentHashMap<String, Integer> cache) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ResultSet results = null;
    Integer vertical = 0;

    String inStmt = "SELECT verticalRise From LiftRides WHERE dayId=? AND skierId=?";
    String key = convertString(dayId, skierId);
    if (cache.containsKey(key)) {
      return cache.get(key);
    }

    try {
      conn = dataSource.getConnection();
      preparedStatement = conn
          .prepareStatement(inStmt);
      preparedStatement.setInt(1, dayId);
      preparedStatement.setInt(2, skierId);
      results = preparedStatement.executeQuery();
      while (results.next()) {
        vertical = results.getInt("verticalRise");
      }
      return vertical;
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
    return -1;
  }

  public String convertString(Integer dayId, Integer skierId) {
    return "DayId: " + dayId + ", SkierId: " + skierId;
  }
}