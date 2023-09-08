import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

/**
 * The type Flight key. A compound key of flight mapper output, contains information of flight Id,
 * month and minutes of delay.
 */
public class FlightKey implements WritableComparable<FlightKey> {

  private String airId;
  private String month;
  private String delay;

  /**
   * Instantiates a new Flight key.
   */
  public FlightKey() {
  }

  /**
   * Instantiates a new Flight key.
   *
   * @param airId the air id
   * @param month the month
   * @param delay the delay
   */
  public FlightKey(String airId, String month, String delay) {
    this.airId = airId;
    this.month = month;
    this.delay = delay;
  }

  /**
   * Gets air id.
   *
   * @return the air id
   */
  public String getAirId() {
    return airId;
  }

  /**
   * Sets air id.
   *
   * @param airId the air id
   */
  public void setAirId(String airId) {
    this.airId = airId;
  }

  /**
   * Gets month.
   *
   * @return the month
   */
  public String getMonth() {
    return month;
  }

  /**
   * Sets month.
   *
   * @param month the month
   */
  public void setMonth(String month) {
    this.month = month;
  }

  /**
   * Gets delay.
   *
   * @return the delay
   */
  public String getDelay() {
    return delay;
  }

  /**
   * Sets delay.
   *
   * @param delay the delay
   */
  public void setDelay(String delay) {
    this.delay = delay;
  }

  public int compareTo(FlightKey key) {
    int res = this.airId.compareTo(key.getAirId());
    if (res == 0) {
      int thisMonth = Integer.valueOf(this. month);
      int otherMonth = Integer.valueOf(key.getMonth());
      if (thisMonth < otherMonth) {
        res = -1;
      } else if (thisMonth > otherMonth) {
        res = 1;
      }
    }
    return res;
  }

  public void write(DataOutput dataOutput) throws IOException {
    dataOutput.writeUTF(this.airId);
    dataOutput.writeUTF(String.valueOf(this.month));
    dataOutput.writeUTF(this.delay);
  }

  public void readFields(DataInput dataInput) throws IOException {
    this.airId = dataInput.readUTF();
    this.month = dataInput.readUTF();
    this.delay = dataInput.readUTF();
  }
}
