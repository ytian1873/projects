import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.StringReader;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * The type Record processor that helps read through original csv data file, and process one record
 * into one put for mapper task.
 */
public class RecordProcessor {

  private StringReader stringReader;
  private CSVReader csvReader;
  private static RecordProcessor instance = null;

  private RecordProcessor() throws IOException {

  }

  /**
   * Gets instance.
   *
   * @return the instance
   * @throws IOException the io exception
   */
  public static RecordProcessor getInstance() throws IOException {
    if (instance == null) {
      instance = new RecordProcessor();
    }
    return instance;
  }

  /**
   * Create put put. Parse info from a line of record into a Put. Use flightId + current time stamp
   * as rowKey, so that all record can be inserted into HBase. Put year, cancelled and diverted from
   * input data into verifyInfo column family; month and delay into essentialInfo column family, and
   * other info as a whole string in other column family.
   *
   * @param info the info
   * @return the put
   * @throws IOException the io exception
   * @throws CsvValidationException the csv validation exception
   */
  public Put createPut(String info) throws IOException, CsvValidationException {
    stringReader = new StringReader(info);
    csvReader = new CSVReader(stringReader);
    String[] values = csvReader.readNext();
    String timeStamp = String.valueOf(System.nanoTime());
    String airLineId = values[Constants.ID_IND];

    String key = airLineId + Constants.SEPARATOR + timeStamp;
    Put put = new Put(Bytes.toBytes(key));

    byte[] verifyColumn = Bytes.toBytes(Constants.CF_VERIFY);
    put.addColumn(verifyColumn, Bytes.toBytes(Constants.CL_YEAR),
        Bytes.toBytes(Constants.YEAR_IND));
    put.addColumn(verifyColumn, Bytes.toBytes(Constants.CL_CANCELLED),
        Bytes.toBytes(Constants.CANCELLED_IND));
    put.addColumn(verifyColumn, Bytes.toBytes(Constants.CL_DIVERTED),
        Bytes.toBytes(Constants.DIVERTED_IND));

    byte[] essentialColumn = Bytes.toBytes(Constants.CF_ESSENTIAL);
    put.addColumn(essentialColumn, Bytes.toBytes(Constants.CL_MONTH),
        Bytes.toBytes(Constants.MONTH_IND));
    put.addColumn(essentialColumn, Bytes.toBytes(Constants.CL_DELAY),
        Bytes.toBytes(Constants.DELAY_IND));

    byte[] other = Bytes.toBytes(Constants.CF_OTHER);
    put.addColumn(other, Bytes.toBytes(Constants.CF_OTHER), Bytes.toBytes(info));

    return put;
  }

  /**
   * Gets string reader.
   *
   * @return the string reader
   */
  public StringReader getStringReader() {
    return stringReader;
  }

  /**
   * Sets string reader.
   *
   * @param stringReader the string reader
   */
  public void setStringReader(StringReader stringReader) {
    this.stringReader = stringReader;
  }

  /**
   * Gets csv reader.
   *
   * @return the csv reader
   */
  public CSVReader getCsvReader() {
    return csvReader;
  }

  /**
   * Sets csv reader.
   *
   * @param csvReader the csv reader
   */
  public void setCsvReader(CSVReader csvReader) {
    this.csvReader = csvReader;
  }

  /**
   * Clean up.
   *
   * @throws IOException the io exception
   */
  public void cleanUp() throws IOException {
    stringReader.close();
    csvReader.close();
  }
}
