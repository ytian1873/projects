package hw;

import static hw.Config.TIMEOUT_MSEC;
import static hw.Config.MSG_TYPE_DATA;
import static hw.Config.MSG_TYPE_ACK;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class StopAndWait extends TransportLayer {

  private Semaphore sem;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private volatile int seq;
  private int waitingAck;

  public StopAndWait(NetworkLayer networkLayer) {
    super(networkLayer);
    sem = new Semaphore(1);
    scheduler = Executors.newScheduledThreadPool(1);
    this.seq = 0;
    this.waitingAck = 0;
  }

  @Override
  public void send(byte[] data) throws IOException {
    try {
      sem.acquire();
      byte[] msg = Util.packMsg(MSG_TYPE_DATA, this.seq, data);
      networkLayer.send(msg.clone());

      timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(msg.clone()),
          TIMEOUT_MSEC,
          TIMEOUT_MSEC,
          TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Thread listener = new Thread(() -> {
      while (true) {
        try {
          byte[] ack = networkLayer.recv();
          HashMap<String, byte[]> unpackedMsg = Util.unpackMsg(ack);
          if (Util.ifCheck(MSG_TYPE_ACK, this.seq, unpackedMsg)) {
            timer.cancel(true);
            this.seq = (this.seq + 1) % 2;
            sem.release();
            break;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    listener.start();
    try {
      listener.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public byte[] recv() throws IOException {
    byte[] data = networkLayer.recv();
    HashMap<String, byte[]> unpackedMsg = Util.unpackMsg(data);
    byte[] payload = unpackedMsg.get("Payload");

    byte[] ack;
    if (Util.ifCheck(MSG_TYPE_DATA, this.waitingAck, unpackedMsg)) {
      ack = Util.packMsg(MSG_TYPE_ACK, this.waitingAck, new byte[0]);
      this.waitingAck = (this.waitingAck + 1) % 2;
    } else {
      ack = Util.packMsg(MSG_TYPE_ACK, (this.waitingAck + 1) % 2, new byte[0]);
      payload = "NACK".getBytes();
    }

    networkLayer.send(ack);
    return payload;
  }

  private class RetransmissionTask implements Runnable {

    private byte[] data;

    public RetransmissionTask(byte[] data) {
      this.data = data;
    }

    @Override
    public void run() {
      try {
        networkLayer.send(data.clone());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void close() throws IOException {
    scheduler.shutdownNow();
    super.close();
  }
}
