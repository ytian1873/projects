package hw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class Util {
  public static void log(String msg) {
    System.out.println(msg);
  }

  public static void log(byte[] msg) {
    StringBuilder sb = new StringBuilder();
    for (byte b : msg) {
      sb.append(String.format("%02X", b));
    }
    log(sb.toString());
  }

  // Introduce random bit error to data.
  public static byte[] randomBitError(byte[] data) {
    int i = ThreadLocalRandom.current().nextInt(data.length);
    data[i] = (byte) ~data[i];
    return data;
  }

  public static void main(String... args) {
    if (args.length > 0) {
      for (String arg : args) {
        log(arg + " => " + new String(randomBitError(arg.getBytes())));
      }
      return;
    }
    byte[] data = new byte[3];
    Arrays.fill(data, (byte) 0xFF);
    log(data);
    randomBitError(data);
    log(data);

    Arrays.fill(data, (byte) 0x45);
    log(data);
    randomBitError(data);
    log(data);
  }

  public static boolean ifCheck(int waitingType, int waitingSeq, HashMap<String, byte[]> unpackedMsg)
      throws IOException {
    int type = byteArrToInt(unpackedMsg.get("Type"));
    int seq = byteArrToInt(unpackedMsg.get("SequenceNumber"));
    int checksum = byteArrToInt(unpackedMsg.get("Checksum")) % Config.MAX_SIZE;
    byte[] payload = unpackedMsg.get("Payload");

    int sum = checksum(type, seq, payload);
    return type == waitingType && seq == waitingSeq && sum == checksum;
  }

  public static byte[] packMsg(int type, int seq, byte[] payload) throws IOException {
    byte[] msg = new byte[6+payload.length];
    int checksum = checksum(type, seq, payload);
    System.arraycopy(intToByteArr(type), 0, msg, 0, 2);
    System.arraycopy(intToByteArr(seq), 0, msg, 2, 2);
    System.arraycopy(intToByteArr(checksum), 0, msg, 4, 2);
    System.arraycopy(payload, 0, msg, 6, payload.length);
    return msg;
  }

  public static HashMap<String, byte[]> unpackMsg(byte[] msg) {
    byte[] typeBytes = Arrays.copyOfRange(msg, 0, 2);
    byte[] seqBytes = Arrays.copyOfRange(msg, 2, 4);
    byte[] checksumBytes = Arrays.copyOfRange(msg, 4, 6);
    byte[] payloadBytes = Arrays.copyOfRange(msg, 6, msg.length);

    HashMap<String, byte[]> unpackedMsg = new HashMap<>();
    unpackedMsg.put("Type", typeBytes);
    unpackedMsg.put("SequenceNumber", seqBytes);
    unpackedMsg.put("Checksum", checksumBytes);
    unpackedMsg.put("Payload", payloadBytes);

    return unpackedMsg;
  }

  public static int checksum(int type, int seq, byte[] payload) throws IOException {
    byte[] typeBytes = intToByteArr(type);
    byte[] seqBytes = intToByteArr(seq);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(6 + payload.length);
    byteArrayOutputStream.write(typeBytes);
    byteArrayOutputStream.write(seqBytes);
    byteArrayOutputStream.write(payload);
    byteArrayOutputStream.flush();

    byte[] bytes = byteArrayOutputStream.toByteArray();

    Checksum checksumEngine = new Adler32();
    checksumEngine.update(bytes, 0, bytes.length);
    long checksum = checksumEngine.getValue();

    return (int) (checksum % Config.MAX_SIZE);
  }

  public static byte[] intToByteArr(int num) {
    return new byte[] {(byte)((num >> 8) & 0x00FF), (byte)((num >> 0) & 0x00FF)};
  }

  public static int byteArrToInt(byte[] byteArr) {
    return  ((byteArr[0] << 8) & 0xFF00) | (byteArr[1] & 0x00FF);
  }
}
