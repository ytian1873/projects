import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class RateMapperKey implements WritableComparable<RateMapperKey> {

  private Integer paramOne;
  private Integer paramTwo;

  public RateMapperKey() {
    setParamOne(-1);
    setParamTwo(-1);
  }

  public RateMapperKey(Integer paramOne, Integer paramTwo) {
    this.paramOne = paramOne;
    this.paramTwo = paramTwo;
  }

  public Integer getParamOne() {
    return paramOne;
  }

  public void setParamOne(Integer paramOne) {
    this.paramOne = paramOne;
  }

  public Integer getParamTwo() {
    return paramTwo;
  }

  public void setParamTwo(Integer paramTwo) {
    this.paramTwo = paramTwo;
  }

  public int compareTo(RateMapperKey o) {
    if (this.paramOne.equals(o.paramOne)) {
      return this.paramTwo.compareTo(o.paramTwo);
    } else {
      return this.paramOne.compareTo(o.paramOne);
    }
  }

  public void write(DataOutput dataOutput) throws IOException {
    dataOutput.writeInt(paramOne);
    dataOutput.writeInt(paramTwo);
  }

  public void readFields(DataInput dataInput) throws IOException {
    this.paramOne = dataInput.readInt();
    this.paramTwo = dataInput.readInt();
  }
}
