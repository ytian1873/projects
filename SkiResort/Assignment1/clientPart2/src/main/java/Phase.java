import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * The type Phase.
 */
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
  private ConcurrentLinkedQueue<ApiResponse<Void>> responses;
  private ConcurrentLinkedQueue<String> info;
  private CountDownLatch latchA;
  private CountDownLatch latchB;

  /**
   * Instantiates a new Phase.
   *
   * @param skier the skier
   * @param numThreads the num threads
   * @param numSkiers the num skiers
   * @param numLifts the num lifts
   * @param numRuns the num runs
   * @param postRate the post rate
   * @param dayId the day id
   * @param seasonId the season id
   * @param resortId the resort id
   * @param startTime the start time
   * @param endTime the end time
   * @param responses the responses
   * @param info the info
   * @param latchA the latch a
   * @param latchB the latch b
   */
  public Phase(SkiersApi skier, Integer numThreads, Integer numSkiers, Integer numLifts,
      Integer numRuns, Double postRate, String dayId, String seasonId, Integer resortId,
      Integer startTime, Integer endTime, ConcurrentLinkedQueue<ApiResponse<Void>> responses,
      ConcurrentLinkedQueue<String> info, CountDownLatch latchA, CountDownLatch latchB) {
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
    this.responses = responses;
    this.info = info;
    this.latchA = latchA;
    this.latchB = latchB;
  }

  /**
   * Start phase.
   */
  public void startPhase() {
    Integer skiersPerThread = this.numSkiers / this.numThreads;
    Double posts = this.numRuns * skiersPerThread * this.postRate;
    Integer postPerThread = posts.intValue();
    for (int i = 0; i < this.numThreads; i++) {
      Thread thread = new Thread(
          new SkierThread(i, this.skier, skiersPerThread, this.numLifts, postPerThread, this.dayId,
              this.seasonId, this.resortId, this.startTime, this.endTime, this.responses, this.info,
              this.latchA, this.latchB));
      thread.start();
    }
  }
}
