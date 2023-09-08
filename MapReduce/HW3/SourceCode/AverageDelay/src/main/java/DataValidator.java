import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The type Data validator. Read, validate, and clean original data.
 */
public class DataValidator {

  private StringReader stringReader;
  private CSVReader csvReader;
  private static DataValidator instance = null;

  private DataValidator() throws IOException {

  }

  /**
   * Gets instance.
   *
   * @return the instance
   * @throws IOException the io exception
   */
  public static DataValidator getInstance() throws IOException {
    if (instance == null) {
      instance = new DataValidator();
    }
    return instance;
  }

  /**
   * Prepare record record. Read a line of record, check if its valid, if so parse the data to Record.
   *
   * @param line the line
   * @return the record
   * @throws Exception the exception
   */
  public Record prepareRecord(String line)
      throws Exception {
    stringReader = new StringReader(line);
    csvReader = new CSVReader(stringReader);
    String[] value = csvReader.readNext();
    String depCity = value[Constants.DEP_IND].trim();
    String arrCity = value[Constants.ARR_IND].trim();
    String cancelled = value[Constants.CANCELLED_IND].trim();
    String diverted = value[Constants.DIVERTED_IND].trim();
    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(value[Constants.DATE_IND].trim());
    String depTime = value[Constants.DEP_TIME_IND].trim();
    String arrTime = value[Constants.ARR_TIME_IND].trim();
    String delay = value[Constants.DELAY_IND].trim();

    // Validate if the line of data is valid. If it is valid, get it's middle city.
    String middle = validateRecord(depCity, arrCity, date, cancelled, diverted);

    // If the data is valid, parse data into a Record.
    if (middle != null) {
      Record record = new Record();
      record.setDate(date);
      // Check if in this line of data there is a delay, if not just set delay to 0
      record.setDelay(validateString(delay) ? Double.valueOf(delay) : 0);
      record.setMiddleCity(middle);
      // Find out this record is the first flight or second flight in the two-legged flight, and set
      // true if it is first, false for second.
      record.setFirstFlight(middle.equals(arrCity));
      // If the flight is first flight, set record time to flight's arrive time, else set to departure time.
      record.setTime(middle.equals(arrCity) ? Integer.valueOf(arrTime) : Integer.valueOf(depTime));
      return record;
    } else {
      return null;
    }
  }

  // Check if the line of data is valid.
  private String validateRecord(String dep, String arr, Date date, String cancelled,
      String diverted) {
    if((dep.equals(Constants.VALID_DEP) || arr.equals(Constants.VALID_ARR)) && cancelled
        .equals(Constants.VALID_STATUS) && diverted.equals(Constants.VALID_STATUS) && date
        .after(Constants.VALID_DATE_LOWER_BOUND) && date.before(Constants.VALID_DATE_UPPER_BOUND)) {
      return dep.equals(Constants.VALID_DEP) ? arr : dep;
    }
    return null;
  }

  // Check if a string is valid.
  private Boolean validateString(String str) {
    return (!str.equals("") && str.length() != 0 && !str.equals(null));
  }

  /**
   * Clean up.
   *
   * @throws IOException the io exception
   */
  public void cleanUp() throws IOException {
    this.stringReader.close();
    this.csvReader.close();
  }

}
