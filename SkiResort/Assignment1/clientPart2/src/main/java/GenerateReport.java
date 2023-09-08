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

/**
 * The type Generate report.
 */
class GenerateReport {

  private ConcurrentLinkedQueue<String> info;

  /**
   * Instantiates a new Generate report.
   *
   * @param info the info
   */
  public GenerateReport(ConcurrentLinkedQueue<String> info) {
    this.info = info;
  }

  /**
   * Write csv file.
   *
   * @return the file
   * @throws IOException the io exception
   */
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
    wr.close();
    writer.close();
    return result;
  }

  /**
   * Do statics.
   *
   * @param file the file
   * @param latency the latency
   * @throws IOException the io exception
   */
  public void doStatics(File file, long latency) throws IOException {
    Long[] arr = getArray(file);
    System.out.println("=============Statistic Results=============");
    System.out.println("Mean response time: " + getMean(arr) + " millisecs");
    System.out.println("Median response time: " + getMedian(arr) + " millisecs");
    System.out.println("Throughput: " + getThroughput(arr, latency) + " millisecs");
    System.out.println("99th percentile: " + get99Percentile(arr) + " millisecs");
    System.out.println("Max response time: " + getMax(arr) + " millisecs");
  }

  /**
   * Get array long [ ].
   *
   * @param file the file
   * @return the long [ ]
   * @throws IOException the io exception
   */
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

  /**
   * Gets median.
   *
   * @param arr the arr
   * @return the median
   */
  public long getMedian(Long[] arr) {
    if (arr.length % 2 == 0) {
      return (arr[arr.length / 2] + arr[arr.length / 2 + 1]) / 2;
    } else {
      return arr[arr.length / 2];
    }
  }

  /**
   * Gets 99 percentile.
   *
   * @param arr the arr
   * @return the 99 percentile
   */
  public long get99Percentile(Long[] arr) {
    Double tempInd = (int) arr.length * 0.99;
    int ind = tempInd.intValue();
    return arr[ind];
  }

  /**
   * Gets max.
   *
   * @param arr the arr
   * @return the max
   */
  public long getMax(Long[] arr) {
    return arr[arr.length - 1];
  }

  /**
   * Gets mean.
   *
   * @param arr the arr
   * @return the mean
   */
  public long getMean(Long[] arr) {
    long total = 0;
    for (int i = 0; i < arr.length; i++) {
      total += arr[i];
    }
    return total / arr.length;
  }

  /**
   * Gets throughput.
   *
   * @param arr the arr
   * @param latency the latency
   * @return the throughput
   */
  public long getThroughput(Long[] arr, long latency) {
    return arr.length / latency;
  }
}
