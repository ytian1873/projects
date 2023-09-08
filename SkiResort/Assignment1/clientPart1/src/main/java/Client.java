import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Client.
 */
public class Client {

  /**
   * The entry point of application. The client sends required number of requests to server, and
   * generate relevant report.
   *
   * @param args the input arguments
   * @throws InterruptedException the interrupted exception
   */
  public static void main(String[] args) throws InterruptedException {
    String ADDRESS_OF_SERVER = "http://184.73.8.63:8080/SkierServlet";
    final int NUM_THREADS = 256;
    final int NUM_SKIERS = 200;
    final int NUM_LIFTS = 40;
    final int NUM_RUNS = 20;
    final double PHASE_ONE_OR_THREE = 0.1;
    final double PHASE_TWO = 0.8;
    final int RESORT_ID = 1;
    final String DAY_ID = "1";
    final String SEASON_ID = "1";

    int success = 0;
    int failure = 0;

    SkiersApi skier = new SkiersApi();
    ApiClient client = skier.getApiClient();
    client.setBasePath(ADDRESS_OF_SERVER);
    ConcurrentLinkedQueue<ApiResponse<Void>> responses = new ConcurrentLinkedQueue<>();

    CountDownLatch latch1A = new CountDownLatch(NUM_THREADS / (4 * 10));
    CountDownLatch latch1B = new CountDownLatch(NUM_THREADS / 4);
    CountDownLatch latch2A = new CountDownLatch(NUM_THREADS / 10);
    CountDownLatch latch2B = new CountDownLatch(NUM_THREADS);
    CountDownLatch latch3B = new CountDownLatch(NUM_THREADS / 4);

    long startTime = System.currentTimeMillis();

    Phase phaseOne = new Phase(skier, NUM_THREADS / 4, NUM_SKIERS, NUM_LIFTS, NUM_RUNS,
        PHASE_ONE_OR_THREE, DAY_ID, SEASON_ID, RESORT_ID,
        1, 90, responses, latch1A, latch1B);
    Phase phaseTwo = new Phase(skier, NUM_THREADS, NUM_SKIERS, NUM_LIFTS, NUM_RUNS, PHASE_TWO,
        DAY_ID, SEASON_ID, RESORT_ID, 91, 360, responses, latch2A, latch2B);
    Phase phaseThree = new Phase(skier, NUM_THREADS / 4, NUM_SKIERS, NUM_LIFTS, NUM_RUNS,
        PHASE_ONE_OR_THREE, DAY_ID, SEASON_ID, RESORT_ID,
        361, 420, responses, null, latch3B);

    phaseOne.startPhase();
    latch1A.await();
    phaseTwo.startPhase();
    latch2A.await();
    phaseThree.startPhase();

    latch1B.await();
    latch2B.await();
    latch3B.await();

    long endTime = System.currentTimeMillis();
    long latency = endTime - startTime;
    NumberFormat formatter = new DecimalFormat("#0.00");

    Pattern pattern = Pattern.compile("^2");
    while (!responses.isEmpty()) {
      ApiResponse<Void> resp = responses.poll();
      String code = String.valueOf(resp.getStatusCode());
      Matcher matcher = pattern.matcher(code);
      if (matcher.find()) {
        success++;
      } else {
        failure++;
      }
    }

    System.out.println("==========Progress Finished==========");
    System.out.println("Total threads: " + NUM_THREADS);
    System.out.println("Total requests posted: " + (failure + success));
    System.out.println("Execution time: " + formatter.format(latency) + " millisecs.");
    System.out.println("Total successful request sent: " + success);
    System.out.println("Total unsuccessful request sent: " + failure);
  }

}
