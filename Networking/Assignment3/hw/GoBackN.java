package hw;

import static hw.Config.MSG_TYPE_ACK;
import static hw.Config.MSG_TYPE_DATA;
import static hw.Config.TIMEOUT_MSEC;
import static hw.Config.WINDOW_SIZE;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// TODO.
public class GoBackN extends TransportLayer {

  private Semaphore sem;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private ConcurrentLinkedDeque<byte[]> window;
  private static final Object timerLock = new Object();
  private static final Object windowLock = new Object();

  private volatile int base;
  private volatile int seq;
  private int waitingSeq;


  public GoBackN(NetworkLayer networkLayer) {
    super(networkLayer);
    this.sem = new Semaphore(WINDOW_SIZE);
    this.scheduler = Executors.newScheduledThreadPool(1);
    this.window = new ConcurrentLinkedDeque<>();
    this.base = 0;
    this.seq = 0;
    this.waitingSeq = 0;
  }

  @Override
  public void send(byte[] data) throws IOException {
    try {
      sem.acquire();
      byte[] msg = Util.packMsg(MSG_TYPE_DATA, this.seq, data);
      synchronized (windowLock) {
        window.offer(msg);
      }
      networkLayer.send(msg.clone());

      if (this.seq == this.base) {
        synchronized (timerLock) {
          timer = scheduler.schedule(new RetransmissionTask(),
              TIMEOUT_MSEC,
              TimeUnit.MILLISECONDS);
        }
      }
      this.seq = (this.seq + 1) % (WINDOW_SIZE + 1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Thread listener = new Thread(() -> {
      while (!window.isEmpty()) {
        try {
          byte[] ack = networkLayer.recv();
          HashMap<String, byte[]> unpackedMsg = Util.unpackMsg(ack);
          int ackType = Util.byteArrToInt(unpackedMsg.get("Type"));
          int ackSeq = Util.byteArrToInt(unpackedMsg.get("SequenceNumber"));
          int ackChecksum = Util.byteArrToInt(unpackedMsg.get("Checksum"));
          byte[] payload = unpackedMsg.get("Payload");

          if (ackType == MSG_TYPE_ACK && ackChecksum == hw.Util
              .checksum(ackType, ackSeq, payload)) {

            int rcvNum = (ackSeq + 1 - base) % (WINDOW_SIZE + 1);
            if (rcvNum <= WINDOW_SIZE) {
              synchronized (windowLock) {
                for (int i = 0; i < rcvNum; i++) {
                  window.poll();
                  sem.release();
                }
              }
              this.base = (ackSeq + 1) % (WINDOW_SIZE + 1);
            }


//            if (ackSeq <= this.base + WINDOW_SIZE) {
  //            while (this.base <= (ackSeq + 1) % (WINDOW_SIZE + 1)) {
  //              synchronized (windowLock) {
  //                window.poll();
  //                sem.release();
  //              }
  //              this.base = (this.base + 1) % (WINDOW_SIZE + 1);
  //            }
//            }
//

            if (this.base == this.seq) {
              synchronized (timerLock) {
                timer.cancel(true);
              }
            }
            synchronized (timerLock) {
              timer = scheduler.schedule(new RetransmissionTask(),
                  TIMEOUT_MSEC,
                  TimeUnit.MILLISECONDS);
            }
          }
          continue;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    listener.start();

    try {
      listener.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public byte[] recv() throws IOException {
    byte[] data = networkLayer.recv();
    HashMap<String, byte[]> unpackedMsg = Util.unpackMsg(data);
    byte[] payload = unpackedMsg.get("Payload");

    byte[] ack;

    System.out.println("waiting " + this.waitingSeq);
    System.out.println("recSeq " + hw.Util.byteArrToInt(unpackedMsg.get("SequenceNumber")));
    if (Util.ifCheck(MSG_TYPE_DATA, this.waitingSeq, unpackedMsg)) {
      ack = Util.packMsg(MSG_TYPE_ACK, this.waitingSeq, new byte[0]);
      this.waitingSeq = (this.waitingSeq + 1) % (WINDOW_SIZE + 1);
    } else {
      ack = Util.packMsg(MSG_TYPE_ACK, (this.waitingSeq - 1) % (WINDOW_SIZE + 1), new byte[0]);
      payload = "NACK".getBytes();
    }

    networkLayer.send(ack);
    return payload;
  }

  private class RetransmissionTask implements Runnable {

    @Override
    public void run() {
      try {
        for (byte[] pck : window) {
          networkLayer.send(pck.clone());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      synchronized (timerLock) {
        timer = scheduler.schedule(new RetransmissionTask(),
            TIMEOUT_MSEC,
            TimeUnit.MILLISECONDS);
      }
    }
  }

  @Override
  public void close() throws IOException {
    scheduler.shutdownNow();
    super.close();
  }
}
