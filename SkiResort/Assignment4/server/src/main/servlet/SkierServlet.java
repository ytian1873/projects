package servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.SkierDao;
import dao.StatDao;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SkierServlet extends javax.servlet.http.HttpServlet {

  private final static int FAILURE = 400;
  private final static int NOT_FOUND = 404;
  private SkierDao skierDao;
  private StatDao statDao;

  @Override
  public void init() throws ServletException {
    this.skierDao = new SkierDao();
    this.statDao = new StatDao();
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws javax.servlet.ServletException, IOException {
    long startTime = System.currentTimeMillis();
    long endTime;
    long latency;
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      endTime = System.currentTimeMillis();
      latency = endTime - startTime;
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Missing Parameters");
      response.getWriter().close();
      this.statDao.insertInfo(startTime, "POST", latency, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String[] parts = urlPath.split("/");

    if (!validUrl(parts)) {
      endTime = System.currentTimeMillis();
      latency = endTime - startTime;
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Invalid URL");
      this.statDao.insertInfo(startTime, "POST", latency, HttpServletResponse.SC_NOT_FOUND);

    } else {
      BufferedReader rd = request.getReader();
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }

      JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
      Integer time = jsonObject.get("time").getAsInt();
      Integer liftID = jsonObject.get("liftID").getAsInt();
      Integer verticalRise = liftID * 10;
      this.skierDao.createLiftRide(Integer.valueOf(parts[8]), Integer.valueOf(parts[2]),
          Integer.valueOf(parts[4]), Integer.valueOf(parts[6]), time, liftID, verticalRise);
      endTime = System.currentTimeMillis();
      latency = endTime - startTime;

      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().write("Success");
      response.getWriter().close();

      this.statDao.insertInfo(startTime, "POST", latency, HttpServletResponse.SC_OK);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    long startTime = System.currentTimeMillis();
    long endTime;
    long latency;

    if (urlPath == null || urlPath.isEmpty()) {
      endTime = System.currentTimeMillis();
      latency = endTime - startTime;
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().print(FAILURE);
      response.getWriter().close();
      this.statDao.insertInfo(startTime, "GET", latency, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String[] parts = urlPath.split("/");

    if (!validUrl(parts)) {
      endTime = System.currentTimeMillis();
      latency = endTime - startTime;
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().print(NOT_FOUND);
      this.statDao.insertInfo(startTime, "GET", latency, HttpServletResponse.SC_NOT_FOUND);
    } else {
      Integer totalVerticals = this.skierDao
          .getVerticalForSpecificDay(Integer.valueOf(parts[6]), Integer.valueOf(parts[8]));

      endTime = System.currentTimeMillis();
      latency = endTime - startTime;
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().print(totalVerticals);
      this.statDao.insertInfo(startTime, "GET", latency, HttpServletResponse.SC_OK);
    }
    response.getWriter().close();
  }

  private boolean validUrl(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if (urlPath.length != 9) {
      return false;
    }

    Pattern pattern = Pattern.compile("^[1-9]d*|0$");
    Matcher isNum1 = pattern.matcher(urlPath[2]);
    Matcher isNum2 = pattern.matcher(urlPath[4]);
    Matcher isNum3 = pattern.matcher(urlPath[6]);
    Matcher isNum4 = pattern.matcher(urlPath[8]);

    return (urlPath[3].equals("seasons") && urlPath[5].equals("days")
        && urlPath[7].equals("skiers") && isNum1.matches() && isNum2.matches() && isNum3.matches()
        && isNum4.matches());
//    return true;
  }
}
