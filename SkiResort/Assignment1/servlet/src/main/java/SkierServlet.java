import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SkierServlet extends javax.servlet.http.HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws javax.servlet.ServletException, IOException {
    response.setContentType("text/plain");
    String path = request.getPathInfo();

    if (path == null || path.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] parts = path.split("/");

    if (!isUrlValid(parts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Invalid URL");
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().write("Success.");
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Invalid URL");
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      response.getWriter().write("It works!");
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]

    Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
    Matcher isNum1 = pattern.matcher(urlPath[1]);
    Matcher isNum2 = pattern.matcher(urlPath[3]);
    Matcher isNum3 = pattern.matcher(urlPath[5]);
    Matcher isNum4 = pattern.matcher(urlPath[7]);
    return (urlPath.length == 8 && urlPath[2].equals("seasons") && urlPath[4].equals("day")
        && urlPath[6].equals("skiers") && isNum1.matches() && isNum2.matches() && isNum3.matches()
        && isNum4.matches());
  }

}
