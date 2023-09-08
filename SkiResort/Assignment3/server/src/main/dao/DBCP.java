package dao;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBCP {

  private static BasicDataSource dataSource;
  private static final String USERNAME = "root";
  private static final String PASSWORD = "Dt1992..";

  static {
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    dataSource = new BasicDataSource();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
//      String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    String url = "jdbc:mysql://34.82.239.160:3306/SkierDB?user=root";
//    String url = "jdbc:mysql://localhost:3306/SkiResort?user=root";

    dataSource.setUrl(url);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setInitialSize(10);
    dataSource.setMaxTotal(60);
  }

  public static BasicDataSource getDataSource() {
    return dataSource;
  }

}
