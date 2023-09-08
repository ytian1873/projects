import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

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
  private ConcurrentLinkedQueue<String> info;
  private CountDownLatch latchA;
  private CountDownLatch latchB;
  private Boolean finalState;
  private AtomicInteger successReq;

  public SkierThread(Integer threadId, SkiersApi skier, Integer skiersPerThread, Integer numLifts,
      Integer postPerThread, String dayId, String seasonId, Integer resortId, Integer startTime,
      Integer endTime, ConcurrentLinkedQueue<String> info, CountDownLatch latchA,
      CountDownLatch latchB, Boolean finalState, AtomicInteger successReq) {
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
    this.info = info;
    this.latchA = latchA;
    this.latchB = latchB;
    this.finalState = finalState;
    this.successReq = successReq;
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
        long postStart = System.currentTimeMillis();
        try {
          ApiResponse<Void> postRes = this.skier
              .writeNewLiftRideWithHttpInfo(ride, this.resortId, this.seasonId, this.dayId,
                  skierId);
          long postEnd = System.currentTimeMillis();
          long postLat = postEnd - postStart;
          if (postRes.getStatusCode() == 200) this.successReq.getAndIncrement();
          String postInfo = postStart + ","
              + "POST" + ","
              + postLat + ","
              + postRes.getStatusCode();
          this.info.offer(postInfo);
          System.out.println(postInfo);
        } catch (ApiException e) {
          e.printStackTrace();
        }

        if (finalState) {
          long getStart = System.currentTimeMillis();
          try {
            ApiResponse<Integer> getRes = this.skier
                .getSkierDayVerticalWithHttpInfo(this.resortId, this.seasonId, this.dayId, skierId);
            long getEnd = System.currentTimeMillis();
            long getLat = getEnd - getStart;
            if (getRes.getStatusCode() == 200) this.successReq.getAndIncrement();
            String getInfo = postStart + ","
                + "GET" + ","
                + getLat + ","
                + getRes.getStatusCode();
            this.info.offer(getInfo);
            System.out.println(getInfo);
          } catch (ApiException e) {
            e.printStackTrace();
          }
        }
      }
    }
    if (latchA != null) {
      latchA.countDown();
    }
    if (latchB != null) {
      latchB.countDown();
    }
  }
}

