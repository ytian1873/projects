package servlet;

import com.google.gson.Gson;
import dao.StatDao;
//import io.swagger.client.model.APIStats;
//import io.swagger.client.model.APIStatsEndpointStats;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatServlet extends javax.servlet.http.HttpServlet {

  private StatDao statDao;

  @Override
  public void init() throws ServletException {
    this.statDao = new StatDao();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
//    resp.setContentType("application/json");
//    String urlPath = req.getPathInfo();
//    APIStats stats = new io.swagger.client.model.APIStats();
//    Gson gson = new Gson();
//
//    if (urlPath == null || urlPath.isEmpty()) {
//      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//      resp.getWriter().print(gson.toJson(stats));
//      resp.getWriter().close();
//      return;
//    }
//
//    Integer mean = this.statDao.getMean().intValue();
//    Integer max = this.statDao.getMax().intValue();
//    APIStatsEndpointStats endpointStats = new APIStatsEndpointStats();
//    endpointStats.setURL(urlPath);
//    endpointStats.setOperation("GET");
//    endpointStats.setMean(mean);
//    endpointStats.setMax(max);
//    stats.addEndpointStatsItem(endpointStats);
//
//    resp.setStatus(HttpServletResponse.SC_OK);
//    resp.getWriter().print(gson.toJson(stats));
//    resp.getWriter().close();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
  }

}
