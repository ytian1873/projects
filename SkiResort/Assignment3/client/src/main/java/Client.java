import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

  public static void main(String[] args) throws InterruptedException, IOException, ApiException {
    String ADDRESS = "https://rosy-stronghold-259620.appspot.com/skiers";
//    String ADDRESS = "http://localhost:8080/server_war_exploded/skiers/";

    final int NUM_THREADS = 256;
    final int NUM_SKIERS = 20000;
    final int NUM_LIFTS = 40;
    final int NUM_RUNS = 20;
    final double PHASE_ONE_OR_THREE = 0.1;
    final double PHASE_TWO = 0.8;
    final int RESORT_ID = 1;
    final int TOTAL_REQ = 440000;
    final String DAY_ID = String.valueOf(ThreadLocalRandom.current().nextInt(1, 365));
    final String SEASON_ID = String.valueOf(ThreadLocalRandom.current().nextInt(2000, 2019));

    AtomicInteger success = new AtomicInteger(0);


    SkiersApi skier = new SkiersApi();
    ApiClient client = skier.getApiClient();
    client.setBasePath(ADDRESS);
    ConcurrentLinkedQueue<String> info = new ConcurrentLinkedQueue<>();

    CountDownLatch latch1A = new CountDownLatch(NUM_THREADS / (4 * 10));
    CountDownLatch latch1B = new CountDownLatch(NUM_THREADS / 4);
    CountDownLatch latch2A = new CountDownLatch(NUM_THREADS / 10);
    CountDownLatch latch2B = new CountDownLatch(NUM_THREADS);
    CountDownLatch latch3B = new CountDownLatch(NUM_THREADS / 4);

    long startTime = System.currentTimeMillis();


    Phase phaseOne = new Phase(skier, NUM_THREADS / 4, NUM_SKIERS, NUM_LIFTS, NUM_RUNS,
        PHASE_ONE_OR_THREE, DAY_ID, SEASON_ID, RESORT_ID,
        1, 90, info, latch1A, latch1B, false, success);

    Phase phaseTwo = new Phase(skier, NUM_THREADS, NUM_SKIERS, NUM_LIFTS, NUM_RUNS, PHASE_TWO,
        DAY_ID, SEASON_ID, RESORT_ID, 91, 360,
        info, latch2A, latch2B, false, success);


    Phase phaseThree = new Phase(skier, NUM_THREADS / 4, NUM_SKIERS, NUM_LIFTS, NUM_RUNS,
        PHASE_ONE_OR_THREE, DAY_ID, SEASON_ID, RESORT_ID,
        361, 420, info, null, latch3B, true, success);

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

    System.out.println("=============Progress Finished=============");
    System.out.println("Total threads: " + NUM_THREADS);
    System.out.println("Total requests posted: " + TOTAL_REQ);
    System.out.println("Execution time: " + latency + " millisecs");
    System.out.println("Total successful request sent: " + success);
    System.out.println();

    GenerateReport ge = new GenerateReport(info);
    File res = ge.writeCsv();
    ge.doStatics(res, latency);
  }
}
