import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/**
 * The type H populate. A Map-Only task that Read through original csv data file and parse data into
 * HBase table.
 */
public class HPopulate {

  /**
   * The type Record mapper. Parse each line of record from csv file into HBase. HBase table has
   * three column families, verifyInfo(including three columns: year, cancelled, diverted),
   * essentialInfo(including month and delay), and other(including a string of all other fields).
   */
  public static class RecordMapper extends Mapper<Object, Text, ImmutableBytesWritable, Put> {

    private RecordProcessor processor;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      processor = RecordProcessor.getInstance();
    }

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String info = value.toString();
      try {
        Put put = processor.createPut(info);
        context.write(null, put);
      } catch (CsvValidationException e) {
        e.printStackTrace();
      }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      processor.cleanUp();
    }
  }

  /**
   * Create a HBase table with designed 3 column families .
   *
   * @param tableName the table name
   * @throws IOException the io exception
   */
  public static void createTable(String tableName) throws IOException {
    HTableDescriptor htd = new HTableDescriptor(tableName);
    HColumnDescriptor verifyCol = new HColumnDescriptor(Constants.CF_VERIFY);
    HColumnDescriptor essentialInfo = new HColumnDescriptor(Constants.CF_ESSENTIAL);
    HColumnDescriptor other = new HColumnDescriptor(Constants.CF_OTHER);
    htd.addFamily(verifyCol);
    htd.addFamily(essentialInfo);
    htd.addFamily(other);
    Configuration conf = HBaseConfiguration.create();
    HBaseAdmin admin = new HBaseAdmin(conf);
    if (admin.tableExists(tableName)) {
      admin.disableTable(tableName);
      admin.deleteTable(tableName);
    }
    admin.createTable(htd);
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
    String tableName = Constants.TABLE_NAME;
    Configuration conf = new Configuration();
    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName);
    createTable(tableName);
    Job job = new Job(conf, "HPopulate");
    job.setJarByClass(HPopulate.class);
    job.setMapperClass(RecordMapper.class);
//    job.setReducerClass(RecordReducer.class);
    job.setNumReduceTasks(0);
    job.setOutputKeyClass(ImmutableBytesWritable.class);
    job.setOutputValueClass(Put.class);
//
//    job.setMapOutputKeyClass(Text.class);
//    job.setMapOutputValueClass(Text.class);
    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TableOutputFormat.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
