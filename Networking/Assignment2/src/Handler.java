import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;

/**
 * The Handler for a single thread.
 */
public class Handler implements Runnable {

  private Socket clientSocket;
  private ArrayList<LocalDateTime> evalRecords;
  private ArrayList<LocalDateTime> gettimeRecords;
  private Queue<String> expressions;

  /**
   * Instantiates a new Handler.
   *
   * @param clientSocket the client socket
   * @param evalRecords the eval api call records
   * @param gettimeRecords the gettime api call records
   * @param expressions the expressions from eval api
   */
  public Handler(Socket clientSocket, ArrayList<LocalDateTime> evalRecords,
      ArrayList<LocalDateTime> gettimeRecords, Queue<String> expressions) {
    this.clientSocket = clientSocket;
    this.evalRecords = evalRecords;
    this.gettimeRecords = gettimeRecords;
    this.expressions = expressions;
  }

  @Override
  public void run() {
    String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
    System.out.println(String.format("Handle client %s", client));
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream()));
      PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

      String statusLine = reader.readLine();
      if (isValid(statusLine)) {
        // Read from status line first to determine which api to deal with.
        String[] info = statusLine.split(" ");
        String method = info[0];
        String api = info[1];
        String httpVersion = info[2];

        HashMap<String, String> headers = new HashMap<>();

        String header;
        while ((header = reader.readLine()) != null) {
          if (header.isEmpty()) {
            break;
          }
          headers.put(header.split(": ")[0], header.split(": ")[1]);
        }

        String response = generateResponse(api, method, httpVersion, reader, headers);
        writer.write(response);
        writer.flush();

        countRecord(api, method);
      }

      System.out.println(String.format("Bye bye %s", client));
      clientSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Generate response string from requests.
   *
   * @param api the api
   * @param method the method
   * @param httpVersion the http version
   * @param reader the reader
   * @return the string
   * @throws Exception the exception
   */
  public String generateResponse(String api, String method, String httpVersion,
      BufferedReader reader, HashMap<String, String> headers)
      throws Exception {
    String response = httpVersion + " " + Utils.STATUS_SUCCESS + Utils.CONTENT_TYPE;
    String result;

    if (api.equals(Utils.EVAL_EXPRESSION) && method.equals(Utils.POST)) {
      int contentLength = Integer.valueOf(headers.get(Utils.CONTENT_HEADER));
      char[] content = new char[contentLength-1];
      reader.read(content, 0, contentLength-1);
      String expression = String.valueOf(content);

//      String expression = reader.readLine();
      result = evalExpression(expression, httpVersion);
      addExpression(expression);
      if (result.equals(httpVersion + " " + Utils.STATUS_BAD_REQUEST)) {
        return result;
      }
    } else if (api.equals(Utils.GET_TIME) && method.equals(Utils.GET)) {
      result = getTime();
    } else if (api.equals(Utils.STATUS_PAGE) && method.equals(Utils.GET)) {
      result = getStatusPage();
    } else {
      return httpVersion + " " + Utils.STATUS_NOT_FOUND;
    }

    response += (Utils.CONTENT_LENGTH + result.length() + Utils.RETURN_MARK + Utils.RETURN_MARK);
    response += result + Utils.RETURN_MARK;

    return response;
  }


  /**
   * Generate result for the eval api.
   *
   * @param expression the expression
   * @return the string
   */
  public String evalExpression(String expression, String httpVersion) {
    int len = expression.length(), sign = 1, result = 0;
    Stack<Integer> stack = new Stack<>();
    for (int i = 0; i < len; i++) {
      if (Character.isDigit(expression.charAt(i))) {
        int sum = expression.charAt(i) - '0';
        while (i + 1 < len && Character.isDigit(expression.charAt(i + 1))) {
          sum = sum * 10 + expression.charAt(i + 1) - '0';
          i++;
        }
        result += sum * sign;
      } else if (expression.charAt(i) == '+') {
        sign = 1;
      } else if (expression.charAt(i) == '-') {
        sign = -1;
      } else if (expression.charAt(i) == '(') {
        stack.push(result);
        stack.push(sign);
        result = 0;
        sign = 1;
      } else if (expression.charAt(i) == ')') {
        result = result * stack.pop() + stack.pop();
      } else {
        return httpVersion + " " + Utils.STATUS_BAD_REQUEST;
      }

    }
    return String.valueOf(result);
  }

  /**
   * Generate result for the gettime api.
   *
   * @return the time
   */
  public String getTime() {
    long millis = System.currentTimeMillis();
    Date date = new java.util.Date(millis);
    return date.toString();
  }

  /**
   * Generate result for status.html.
   *
   * @return the status page
   * @throws Exception the exception
   */
  public String getStatusPage() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("<h1>API count information</h1>\r\n");
    sb.append("<h3>/api/evalexpression</h3>\r\n");
    sb.append("<ul>\r\n");
    sb.append("<li>last minute: ").append(getPrevRecords(this.evalRecords, Utils.MIN))
        .append("</li>\r\n");
    sb.append("<li>last hour: ").append(getPrevRecords(this.evalRecords, Utils.HOUR))
        .append("</li>\r\n");
    sb.append("<li>last 24 hours: ").append(getPrevRecords(this.evalRecords, Utils.DAY))
        .append("</li>\r\n");
    sb.append("<li>lifetime: ").append(this.evalRecords.size()).append("</li>\r\n");
    sb.append("</ul>\r\n");

    sb.append("<h3>/api/gettime</h3>");
    sb.append("<ul>\r\n");
    sb.append("<li>last minute: ").append(getPrevRecords(this.gettimeRecords, Utils.MIN))
        .append("</li>\r\n");
    sb.append("<li>last hour: ").append(getPrevRecords(this.gettimeRecords, Utils.HOUR))
        .append("</li>\r\n");
    sb.append("<li>last 24 hours: ").append(getPrevRecords(this.gettimeRecords, Utils.DAY))
        .append("</li>\r\n");
    sb.append("<li>lifetime: ").append(this.gettimeRecords.size()).append("</li>\r\n");
    sb.append("</ul>\r\n");

    sb.append("<h1>Last 10 expressions</h1>");
    sb.append("<ul>\r\n");
    for (String expression : this.expressions) {
      sb.append("<li>").append(expression).append("</li>\r\n");
    }
    sb.append("</ul>");

    return sb.toString();
  }


  /**
   * Check if the line read from buffered reader is valid.
   *
   * @param str the str
   * @return the boolean
   */
  public boolean isValid(String str) {
    return (str != null && !str.equals("") && !(str.length() == 0));
  }

  /**
   * Do statistics for the api calls.
   *
   * @param api the api
   * @param method the method
   */
  public void countRecord(String api, String method) {
    ArrayList<LocalDateTime> curRecord;
    if (api.equals(Utils.EVAL_EXPRESSION) && method.equals(Utils.POST)) {
      curRecord = this.evalRecords;
    } else if (api.equals(Utils.GET_TIME) && method.equals(Utils.GET)) {
      curRecord = this.gettimeRecords;
    } else {
      return;
    }

    curRecord.add(LocalDateTime.now());
  }

  /**
   * Add expression to statistics. Maximum 10 records.
   *
   * @param expression the expression
   */
  public void addExpression(String expression) {
    if (this.expressions.size() >= Utils.MAX_EXPRESSION_NUM) {
      this.expressions.poll();
    }
    this.expressions.offer(expression);
  }

  /**
   * Gets previous api call records.
   *
   * @param record the record
   * @param timeRange the time range
   * @return the prev day records
   * @throws Exception the exception
   */
  public int getPrevRecords(ArrayList<LocalDateTime> record, String timeRange) throws Exception {
    int total = 0;
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastDay;
    if (timeRange.equals(Utils.DAY)) {
      lastDay = now.minusDays(1);
    } else if (timeRange.equals(Utils.HOUR)) {
      lastDay = now.minusHours(1);
    } else if (timeRange.equals(Utils.MIN)) {
      lastDay = now.minusMinutes(1);
    } else {
      throw new Exception("Invalid Time Range Input");
    }

    for (int i = record.size() - 1; i >= 0; i--) {
      LocalDateTime time = record.get(i);
      if (time.isAfter(lastDay)) {
        total += 1;
      } else {
        break;
      }
    }
    return total;
  }

}
