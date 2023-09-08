import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.StringReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * The type Secondary sort.
 */
public class SecondarySort {

  /**
   * The type Flight mapper. Read from original csv data file, and parse data into designed format,
   * which is using flightId and month as a compound key for output, and delay as value for output.
   */
  public static class FlightMapper extends Mapper<Object, Text, FlightKey, Text> {

    private CSVReader csvReader;
    private StringReader strReader;

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String info = value.toString();
      try {
        FlightKey outKey = validate(info);
        if (outKey != null) {
          Text outValue = new Text(outKey.getDelay());
          context.write(outKey, outValue);
        }
      } catch (CsvValidationException e) {
        e.printStackTrace();
      }
    }

    /**
     * Helper method that helps mapper read from data, and valid if the line of record is valid to
     * parse to reducer. If the record is valid, put all information into a FlightKey and return
     * it.
     */
    private FlightKey validate(String info) throws IOException, CsvValidationException {
      strReader = new StringReader(info);
      csvReader = new CSVReader(strReader);
      String[] values = csvReader.readNext();
      String id = values[Constants.ID_IND];
      String year = values[Constants.YEAR_IND];
      String month = values[Constants.MONTH_IND];
      String canceled = values[Constants.CANCELED_IND];
      String derived = values[Constants.DERIVED_IND];
      String delay = values[Constants.DELAY_IND];

      // If any of the necessary info is blank in record, ignore that line.
      if (validString(id) || validString(year) || validString(month) ||
          validString(canceled) || validString(derived) || validString(delay)) {
        return null;
      }

      // If flight year is not 2008, or it is cancelled or diverted, then ignore the record.
      if (!year.equals(Constants.VALID_YEAR) || !canceled.equals(Constants.VALID_STATUS) ||
          !derived.equals(Constants.VALID_STATUS)) {
        return null;
      }
      return new FlightKey(id, month, delay);
    }

    /**
     * Helper function that helps to check if any info from the record is blank.
     */
    private Boolean validString(String str) {
      return str == null || str.equals("") || str.length() == 0;
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      strReader.close();
      csvReader.close();
    }
  }

  /**
   * The type Group comparator. Combines FlightKey with same flightId into same group.
   */
  public static class GroupComparator extends WritableComparator {

    /**
     * Instantiates a new Group comparator.
     */
    public GroupComparator() {
      super(FlightKey.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
      FlightKey keyA = (FlightKey) a;
      FlightKey keyB = (FlightKey) b;
      return keyA.getAirId().compareTo(keyB.getAirId());
    }
  }

  /**
   * The type Sort comparator. Sorts FlightKey with ascending order.
   */
  public static class SortComparator extends WritableComparator {

    /**
     * Instantiates a new Sort comparator.
     */
    public SortComparator() {
      super(FlightKey.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
      FlightKey keyA = (FlightKey) a;
      FlightKey keyB = (FlightKey) b;
      return keyA.compareTo(keyB);
    }
  }

  /**
   * The type Flight reducer. Aggregate data from mapper, and calculate monthly average delay for
   * flights.
   */
  public static class FlightReducer extends Reducer<FlightKey, Text, Text, Text> {

    @Override
    protected void reduce(FlightKey key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {

      // Initiate a double array which length is 12, to store each month's total delay and
      // counts of delay for each flight.
      Double[][] monthDelay = new Double[Constants.MONTHS][2];

      for (int i = 0; i < Constants.MONTHS; i++) {
        monthDelay[i][0] = 0.0;
        monthDelay[i][1] = 0.0;
      }

      // Aggregate delay info from mapper.
      for (Text value : values) {
        int month = Integer.valueOf(key.getMonth());
        double delay = Double.valueOf(value.toString());
        monthDelay[month - 1][0] += 1.0;
        monthDelay[month - 1][1] += delay;
      }

      // Calculate monthly average delay, and use stringBuilder to format.
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < Constants.MONTHS; i++) {
        int delay = (int) Math.ceil(monthDelay[i][1] / monthDelay[i][0]);
        stringBuilder.append(Constants.LEFT).append(i + 1).append(Constants.SEPARATOR).append(delay)
            .append(Constants.RIGHT);
        if (i != (Constants.MONTHS - 1)) {
          stringBuilder.append(Constants.SEPARATOR);
        }
      }

      String res = stringBuilder.toString();
      context.write(new Text(key.getAirId()), new Text(res));
    }
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws IOException the io exception
   * @throws ClassNotFoundException the class not found exception
   * @throws InterruptedException the interrupted exception
   */
  public static void main(String[] args)
      throws IOException, ClassNotFoundException, InterruptedException {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "secondary sort");
    job.setJarByClass(SecondarySort.class);
    job.setMapperClass(FlightMapper.class);
    job.setReducerClass(FlightReducer.class);
    job.setGroupingComparatorClass(GroupComparator.class);
    job.setSortComparatorClass(SortComparator.class);
    job.setMapOutputKeyClass(FlightKey.class);
    job.setMapOutputValueClass(Text.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
