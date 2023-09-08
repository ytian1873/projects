import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

/**
 * The compound mapper output key. Contains information of flightId and month.
 */
public class ReducerKey implements WritableComparable<ReducerKey> {

  private String flightId;
  private String month;

  /**
   * Instantiates a new Reducer key.
   */
  public ReducerKey() {
  }

  /**
   * Instantiates a new Reducer key.
   *
   * @param flightId the flight id
   * @param month the month
   */
  public ReducerKey(String flightId, String month) {
    this.flightId = flightId;
    this.month = month;
  }

  /**
   * Gets flight id.
   *
   * @return the flight id
   */
  public String getFlightId() {
    return flightId;
  }

  /**
   * Sets flight id.
   *
   * @param flightId the flight id
   */
  public void setFlightId(String flightId) {
    this.flightId = flightId;
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

  public int compareTo(ReducerKey o) {
    if (this.flightId.equals(o.getFlightId())) {
      return this.month.compareTo(o.month);
    }
    return this.flightId.compareTo(o.flightId);
  }

  public void write(DataOutput dataOutput) throws IOException {
    dataOutput.writeUTF(flightId);
    dataOutput.writeUTF(month);
  }

  public void readFields(DataInput dataInput) throws IOException {
    this.flightId = dataInput.readUTF();
    this.month = dataInput.readUTF();
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ReducerKey)) {
      return false;
    }
    return this.flightId.equals(((ReducerKey) obj).getFlightId()) && this.month
        .equals(((ReducerKey) obj).getMonth());
  }
}
