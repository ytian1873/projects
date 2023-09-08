package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class SkierDao {

  private static BasicDataSource dataSource;


  public SkierDao() {
    dataSource = DBCP.getDataSource();
  }

  public void createLiftRide(Integer skierId, Integer resortId, Integer seasonId, Integer dayId,
      Integer liftTime, Integer liftId, Integer verticalRise) {
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

  public Integer getVerticalForSpecificDay(Integer dayId, Integer skierId) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ResultSet results = null;
    Integer totalVertical = 0;

    String inStmt = "SELECT verticalRise From LiftRides WHERE dayId=? AND skierId=?";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn
          .prepareStatement(inStmt);
      preparedStatement.setInt(1, dayId);
      preparedStatement.setInt(2, skierId);
      results = preparedStatement.executeQuery();
      while (results.next()) {
        int vertical = results.getInt("verticalRise");
        totalVertical += vertical;
      }
      return totalVertical;
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
}