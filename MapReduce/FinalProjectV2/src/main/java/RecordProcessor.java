import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.StringReader;

public class RecordProcessor {

  private static RecordProcessor instance = null;
  private static StringReader stringReader;
  private static CSVReader csvReader;
  private Integer movieId = -1;

  private RecordProcessor() {
  }

  public static RecordProcessor getInstance() {
    if (instance == null) {
      instance = new RecordProcessor();
    }
    return instance;
  }

  public RateRecord prepareRecord(String line) throws IOException, CsvValidationException {
    stringReader = new StringReader(line);
    csvReader = new CSVReader(stringReader);
    String[] values = csvReader.readNext();
    RateRecord rec = new RateRecord();
    if (values.length == 1) {
      String id = values[0];
      setMovieId(Integer.parseInt(id.substring(0, id.length() - 1)));
    } else if (values.length == 3) {
      rec.setUserId(Integer.parseInt(values[0]));
      rec.setRate((Integer.parseInt(values[1])));
//      rec.setRate((Integer.parseInt(values[1])) > 3 ? 2 : 1);
    }

    return rec;
  }

  public static StringReader getStringReader() {
    return stringReader;
  }

  public static void setStringReader(StringReader stringReader) {
    RecordProcessor.stringReader = stringReader;
  }

  public static CSVReader getCsvReader() {
    return csvReader;
  }

  public static void setCsvReader(CSVReader csvReader) {
    RecordProcessor.csvReader = csvReader;
  }

  public Integer getMovieId() {
    return movieId;
  }

  public void setMovieId(Integer movieId) {
    this.movieId = movieId;
  }

  public void cleanUp() throws IOException {
    stringReader.close();
    csvReader.close();
  }
}
