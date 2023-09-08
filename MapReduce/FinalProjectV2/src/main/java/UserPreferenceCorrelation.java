import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class UserPreferenceCorrelation {

  public static class DataMapper extends Mapper<Object, Text, RateMapperKey, IntWritable> {

    private RecordProcessor processor;
    private Integer movieId;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      processor = RecordProcessor.getInstance();
      movieId = 0;
    }

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String line = value.toString();
      try {
        RateRecord record = processor.prepareRecord(line);
        Integer curId = processor.getMovieId();
        if (!movieId.equals(curId)) {
          movieId = curId;
        } else {
          RateMapperKey rateKey = new RateMapperKey(movieId, record.getRate());
          context.write(rateKey, new IntWritable(record.getUserId()));
        }
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

  public static class PreferenceReducer extends
      Reducer<RateMapperKey, IntWritable, Text, IntWritable> {

    private HashMap<RateMapperKey, IntWritable> pairMap;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      pairMap = new HashMap<RateMapperKey, IntWritable>();
    }

    @Override
    protected void reduce(RateMapperKey key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      ArrayList<Integer> userList = new ArrayList<Integer>();
      for (IntWritable user : values) {
        userList.add(user.get());
      }
      for (int i = 0; i < userList.size(); i++) {
        for (int j = i + 1; j < userList.size(); j++) {
          RateMapperKey newPair = new RateMapperKey(userList.get(i), userList.get(j));
          if (pairMap.containsKey(newPair)) {
            Integer oldVal = pairMap.get(newPair).get();
            pairMap.get(newPair).set(oldVal + 1);
          } else {
            pairMap.put(newPair, new IntWritable(1));
          }
        }
      }
    }


    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      for (Entry<RateMapperKey, IntWritable> entry : pairMap.entrySet()) {
        Text outKey = new Text(
            entry.getKey().getParamOne().toString() + "," + entry.getKey().getParamTwo()
                .toString());
        IntWritable outVal = entry.getValue();
        context.write(outKey, outVal);
      }

    }
  }

  public static class GroupComparator extends WritableComparator {

    public GroupComparator() {
      super(RateMapperKey.class, true);
    }

    @Override
    public int compare(Object a, Object b) {
      RateMapperKey keyA = (RateMapperKey) a;
      RateMapperKey keyB = (RateMapperKey) b;
      return keyA.getParamOne().compareTo(keyB.getParamTwo());
    }
  }

  public static void main(String[] args)
      throws IOException, ClassNotFoundException, InterruptedException {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "FindUserPreferenceCorrelation");
    job.setJarByClass(UserPreferenceCorrelation.class);

    job.setMapperClass(DataMapper.class);
    job.setReducerClass(PreferenceReducer.class);
    job.setGroupingComparatorClass(GroupComparator.class);
    job.setMapOutputKeyClass(RateMapperKey.class);
    job.setMapOutputValueClass(IntWritable.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    String inputPath = args[0] + "," + args[1] + "," + args[2] + "," + args[3];
    FileInputFormat.addInputPaths(job, inputPath);
//    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[4]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
