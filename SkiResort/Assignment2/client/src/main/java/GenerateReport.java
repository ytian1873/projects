import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class GenerateReport {

  private ConcurrentLinkedQueue<String> info;

  public GenerateReport(ConcurrentLinkedQueue<String> info) {
    this.info = info;
  }

  public File writeCsv() throws IOException {
    String currWorkingDir = System.getProperty("user.dir");
    File result = new File(currWorkingDir + "/" + "result.csv");
    FileOutputStream stream = new FileOutputStream(result);
    OutputStreamWriter wr = new OutputStreamWriter(stream, "UTF-8");
    BufferedWriter writer = new BufferedWriter(wr);
    writer.write("StartTime,RequestType,Latency,ResponseCode");
    writer.newLine();
    while (!this.info.isEmpty()) {
      String cur = info.poll();
      writer.write(cur);
      writer.newLine();
      writer.flush();
    }
//    wr.close();
//    writer.close();
    return result;
  }

  public void doStatics(File file, long latency) throws IOException {
    Long[] arr = getArray(file);
    System.out.println("=============Statistic Results=============");
    System.out.println("Mean response time: " + getMean(arr) + " millisecs");
    System.out.println("Median response time: " + getMedian(arr) + " millisecs");
    System.out.println("99th percentile: " + get99Percentile(arr) + " millisecs");
    System.out.println("Max response time: " + getMax(arr) + " millisecs");
    System.out.println("Throughput: " + getThroughput(arr, latency));
  }

  public Long[] getArray(File file) throws IOException {
    FileInputStream stream = new FileInputStream(file);
    InputStreamReader sr = new InputStreamReader(stream, "UTF-8");
    BufferedReader reader = new BufferedReader(sr);
    String line = reader.readLine();
    List<Long> tempRes = new ArrayList<>();
    while ((line = reader.readLine()) != null) {
      String[] cur = line.split(",");
      long res = Long.valueOf(cur[2]);
      tempRes.add(res);
    }
    Long[] arr = new Long[tempRes.size()];
    tempRes.toArray(arr);
    Arrays.sort(arr);
    return arr;
  }

  public long getMedian(Long[] arr) {
    if (arr.length == 0) return 0;
    if (arr.length % 2 == 0) {
      return (arr[arr.length / 2] + arr[arr.length / 2 + 1]) / 2;
    } else {
      return arr[arr.length / 2];
    }
  }

  public long get99Percentile(Long[] arr) {
    if (arr.length == 0) return 0;
    Double tempInd = (int) arr.length * 0.99;
    int ind = tempInd.intValue();
    return arr[ind];
  }

  public long getMax(Long[] arr) {
    if (arr.length == 0) return 0;
    return arr[arr.length - 1];
  }

  public long getMean(Long[] arr) {
    if (arr.length == 0) return 0;
    long total = 0;
    for (int i = 0; i < arr.length; i++) {
      total += arr[i];
    }
    return total / arr.length;
  }

  public long getThroughput(Long[] arr, long latency) {
    return arr.length / (latency / 1000);
  }
}
