import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A single Skier thread.
 */
public class SkierThread implements Runnable {

  private Integer threadId;
  private SkiersApi skier;
  private Integer skiersPerThread;
  private Integer numLifts;
  private Integer postPerThread;
  private String dayId;
  private String seasonId;
  private Integer resortId;
  private Integer startTime;
  private Integer endTime;
  private ConcurrentLinkedQueue<ApiResponse<Void>> responses;
  private CountDownLatch latchA;
  private CountDownLatch latchB;

  /**
   * Instantiates a new Skier thread.
   *
   * @param threadId the thread id
   * @param skier the skier
   * @param skiersPerThread the skiers per thread
   * @param numLifts the num lifts
   * @param postPerThread the post per thread
   * @param dayId the day id
   * @param seasonId the season id
   * @param resortId the resort id
   * @param startTime the start time
   * @param endTime the end time
   * @param responses the responses
   * @param latchA the latch a
   * @param latchB the latch b
   */
  public SkierThread(Integer threadId, SkiersApi skier, Integer skiersPerThread, Integer numLifts,
      Integer postPerThread, String dayId, String seasonId, Integer resortId, Integer startTime,
      Integer endTime, ConcurrentLinkedQueue<ApiResponse<Void>> responses, CountDownLatch latchA,
      CountDownLatch latchB) {
    this.threadId = threadId;
    this.skier = skier;
    this.skiersPerThread = skiersPerThread;
    this.numLifts = numLifts;
    this.postPerThread = postPerThread;
    this.dayId = dayId;
    this.seasonId = seasonId;
    this.resortId = resortId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.responses = responses;
    this.latchA = latchA;
    this.latchB = latchB;
  }

  public void run() {
    synchronized (this) {
      for (int i = 0; i < this.postPerThread; i++) {
        Integer skierId = ThreadLocalRandom.current()
            .nextInt(this.threadId * skiersPerThread + 1,
                this.threadId * skiersPerThread + skiersPerThread);
        Integer randTime = ThreadLocalRandom.current().nextInt(this.startTime, this.endTime);
        Integer rideId = ThreadLocalRandom.current().nextInt(1, this.numLifts);
        LiftRide ride = new LiftRide();
        ride.time(randTime);
        ride.liftID(rideId);
        try {
          ApiResponse<Void> response = this.skier
              .writeNewLiftRideWithHttpInfo(ride, this.resortId, this.seasonId, this.dayId,
                  skierId);
          this.responses.offer(response);
        } catch (ApiException e) {
          e.printStackTrace();
        }
      }
      if (latchA != null) latchA.countDown();
      if (latchB != null) latchB.countDown();
    }
  }
}
