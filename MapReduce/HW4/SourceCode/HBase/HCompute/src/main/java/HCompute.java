import com.google.protobuf.ServiceException;
import java.io.File;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * The main task of HCompute. Read through the HBase to get information, and compute flight monthly
 * delay for output.
 */
public class HCompute {

  /**
   * The type H mapper. Read from HBase table, and emit info containing flightId, month, and delay.
   * Use flightId and month as compound key, delay as value.
   */
  public static class HMapper extends TableMapper<ReducerKey, Text> {

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context)
        throws IOException, InterruptedException {
      String[] inputKey = Bytes.toString(key.get()).split(Constants.SEPARATOR);
      String flightId = inputKey[0];
      String month = Bytes
          .toString(value
              .getValue(Bytes.toBytes(Constants.CF_ESSENTIAL), Bytes.toBytes(Constants.CL_MONTH)));
      String delay = Bytes
          .toString(value
              .getValue(Bytes.toBytes(Constants.CF_ESSENTIAL), Bytes.toBytes(Constants.CL_DELAY)));
      String cancelled = Bytes
          .toString(value
              .getValue(Bytes.toBytes(Constants.CF_VERIFY), Bytes.toBytes(Constants.CL_CANCELLED)));
      String diverted = Bytes
          .toString(value
              .getValue(Bytes.toBytes(Constants.CF_VERIFY), Bytes.toBytes(Constants.CL_DIVERTED)));
      String year = Bytes
          .toString(
              value.getValue(Bytes.toBytes(Constants.CF_VERIFY), Bytes.toBytes(Constants.CL_YEAR)));

      if (year.equals(Constants.REQUIRED_YEAR) && cancelled.equals(Constants.VALID_STATUES)
          && diverted.equals(Constants.VALID_STATUES)) {
        if (validStr(month) && validStr(delay)) {
          context.write(new ReducerKey(flightId, month), new Text(delay));
        }
      }
    }

    /**
     * Helper function that helps to check if info acquired from HBase is validate to be computed in
     * reducer. If the checked string is empty or null, then the record will not be send to reducer
     * for further calculation.
     */
    private Boolean validStr(String str) {
      return !str.equals("") && !(str.length() == 0) && !(str == null);
    }
  }


  /**
   * The type H reducer. Aggregate and calculate average monthly delay data for each flight, and
   * output result with certain format.
   */
  public static class HReducer extends Reducer<ReducerKey, Text, Text, Text> {

    @Override
    protected void reduce(ReducerKey key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {
      Double[][] monthDelay = new Double[Constants.MONTHS][2];

      for (int i = 0; i < Constants.MONTHS; i++) {
        monthDelay[i][0] = 0.0;
        monthDelay[i][1] = 0.0;
      }

      for (Text value : values) {
        int month = Integer.valueOf(key.getMonth());
        double delay = Double.valueOf(value.toString());
        monthDelay[month - 1][0] += 1.0;
        monthDelay[month - 1][1] += delay;
      }

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
      context.write(new Text(key.getFlightId()), new Text(res));
    }
  }


  /**
   * The type Group comparator. Compares two records, and if their flightId are the same, then they
   * will be grouped as a same group.
   */
  public static class GroupComparator extends WritableComparator {

    /**
     * Instantiates a new Group comparator.
     */
    public GroupComparator() {
      super(ReducerKey.class, true);
    }

    @Override
    public int compare(Object a, Object b) {
      String flightIdA = ((ReducerKey) a).getFlightId();
      String flightIdB = ((ReducerKey) b).getFlightId();
      return flightIdA.compareTo(flightIdB);
    }
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws IOException the io exception
   * @throws ClassNotFoundException the class not found exception
   * @throws InterruptedException the interrupted exception
   * @throws ServiceException the service exception
   */
  public static void main(String[] args)
      throws IOException, ClassNotFoundException, InterruptedException, ServiceException {
    Configuration conf = new Configuration();
    String hbaseSite = "/etc/hbase/conf/hbase-site.xml";
    conf.addResource(new File(hbaseSite).toURI().toURL());
    HBaseAdmin.checkHBaseAvailable(conf);

    conf.set(TableInputFormat.INPUT_TABLE, Constants.TABLE_NAME);
    Job job = Job.getInstance(conf, "HCompute");
    job.setJarByClass(HCompute.class);

    Scan scan = new Scan();
    scan.setCacheBlocks(false);
    scan.setCaching(500);
    scan.addFamily(Bytes.toBytes(Constants.CF_VERIFY));
    scan.addFamily(Bytes.toBytes(Constants.CF_ESSENTIAL));

    TableMapReduceUtil.initTableMapperJob(
        Constants.TABLE_NAME,
        scan,
        HMapper.class,
        ReducerKey.class,
        Text.class,
        job
    );

    job.setReducerClass(HReducer.class);
    job.setGroupingComparatorClass(GroupComparator.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    FileOutputFormat.setOutputPath(job, new Path(args[0]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
