import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Phase {

  private SkiersApi skier;
  private Integer numThreads;
  private Integer numSkiers;
  private Integer numLifts;
  private Integer numRuns;
  private Double postRate;
  private String dayId;
  private String seasonId;
  private Integer resortId;
  private Integer startTime;
  private Integer endTime;
  private ConcurrentLinkedQueue<String> info;
  private CountDownLatch latchA;
  private CountDownLatch latchB;
  private Boolean finalState;
  private AtomicInteger successReq;

  public Phase(SkiersApi skier, Integer numThreads, Integer numSkiers, Integer numLifts,
      Integer numRuns, Double postRate, String dayId, String seasonId, Integer resortId,
      Integer startTime, Integer endTime, ConcurrentLinkedQueue<String> info, CountDownLatch latchA,
      CountDownLatch latchB, Boolean finalState, AtomicInteger successReq) {
    this.skier = skier;
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.numLifts = numLifts;
    this.numRuns = numRuns;
    this.postRate = postRate;
    this.dayId = dayId;
    this.seasonId = seasonId;
    this.resortId = resortId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.info = info;
    this.latchA = latchA;
    this.latchB = latchB;
    this.finalState = finalState;
    this.successReq = successReq;
  }

  public void startPhase() {
    Integer skiersPerThread = this.numSkiers / this.numThreads;
    Double posts = this.numRuns * skiersPerThread * this.postRate;
    Integer postPerThread = posts.intValue();
    for (int i = 0; i < this.numThreads; i++) {
      Thread thread = new Thread(
          new SkierThread(i, this.skier, skiersPerThread, this.numLifts, postPerThread, this.dayId,
              this.seasonId, this.resortId, this.startTime, this.endTime, this.info,
              this.latchA, this.latchB, this.finalState, successReq));
      thread.start();
    }
  }
}
