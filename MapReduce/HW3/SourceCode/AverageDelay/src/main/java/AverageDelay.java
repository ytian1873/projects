import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * The main task that finds out valid records and compute average delay for them.
 */
public class AverageDelay {

  /**
   * The type Delay mapper.
   */
  public static class DelayMapper extends Mapper<Object, Text, Text, DoubleWritable> {

    // Use a HashMap for data filtering and combining. Use first flight's
    // dest city code/second flight's dep city code as key, and cleaned record, Record as value.
    // For a new read in record, with a middle city X, if the findMap already has some valid records
    // which dest/dep city is X, then search in the corresponding entry if there is another record
    // that can pair up with this current read in record, if so emit the pair, else put the new
    // record in map.
    private HashMap<String, ArrayList<Record>> findMap;
    private DataValidator validator;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      findMap = new HashMap<String, ArrayList<Record>>();
      validator = DataValidator.getInstance();
    }

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      try {
        Record record = validator.prepareRecord(value.toString());
        if (record != null) {
          String middleCity = record.getMiddleCity();
          Boolean isFirstFlight = record.getFirstFlight();
          if (findMap.containsKey(middleCity)) {
            // If there are already some records that has same middleCity, check if that is valid
            // to be combined as a two-legged record.
            for (Record rec : findMap.get(middleCity)) {
              // Check if the found data and income data are first and second, and if first time
              // before second time.
              if (!rec.getFirstFlight().equals(isFirstFlight) && rec.getDate()
                  .equals(record.getDate()) && ((isFirstFlight && record.getTime() < rec.getTime())
                      || !isFirstFlight && record.getTime() > rec.getTime())) {
                // If there is a two-legged flight, calculate its delay and emit to reducer.
                Double delay = rec.getDelay() + record.getDelay();
                findMap.get(middleCity).remove(rec);
                context.write(new Text("valid"), new DoubleWritable(delay));
              }
            }
          } else {
            // If there is not such a data in findMap that can create a two-legged flight, put the
            // income record in map
            ArrayList<Record> recList = new ArrayList<Record>();
            recList.add(record);
            findMap.put(middleCity, recList);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      findMap.clear();
      validator.cleanUp();
    }
  }

  /**
   * The type Delay reducer. Aggregate data and compute average delay.
   */
  public static class DelayReducer extends Reducer<Text, DoubleWritable, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context)
        throws IOException, InterruptedException {
      double totalDelay = 0.0;
      int count = 0;
      for (DoubleWritable val : values) {
        totalDelay += val.get();
        count++;
      }
      double avgDelay = (count == 0) ? 0 : (totalDelay / count);
      context.write(new Text("Average Delay: "), new Text(String.valueOf(avgDelay)));
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
    Job job = Job.getInstance(conf, "AverageDelay");
    job.setJarByClass(AverageDelay.class);
    job.setMapperClass(DelayMapper.class);
    job.setReducerClass(DelayReducer.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(DoubleWritable.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }

}

