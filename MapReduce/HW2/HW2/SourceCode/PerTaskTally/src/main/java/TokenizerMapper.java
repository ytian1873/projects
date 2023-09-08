import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/* Input type is modified, original use Text, substituted with HashMap<Text, IntWritable> */
public class TokenizerMapper
    extends Mapper<Object, Text, Text, IntWritable> {

  /* In this version, a hash map is created to aggregate. Key is a Text, value is a IntWritable */
  private Text word = new Text();
  private HashMap<String, Integer> wordMap;

  /**
   * Define setup process, and initiate a hashmap.
   */
  protected void setup(Context context) throws IOException, InterruptedException {
    wordMap = new HashMap<String, Integer>();
  }

  public void map(Object key, Text value, Context context
  ) throws IOException, InterruptedException {
    /* To validate strings in the input file, used a char array for qualified range. */
    Character[] chars = {'M', 'm', 'N', 'n', 'O', 'o', 'P', 'p', 'Q', 'q'};
    List<Character> range = new ArrayList<Character>(Arrays.asList(chars));
    StringTokenizer itr = new StringTokenizer(value.toString());

    while (itr.hasMoreTokens()) {
      String next = itr.nextToken();
      Character beginning = next.charAt(0);
      /* For each word, check if the first letter falls into validated range,
       if so send to further work */
      if (range.contains(beginning)) {
        /* Store each valid word into hash map and aggregate */
        if (wordMap.containsKey(next)) {
          wordMap.put(next, 1 + wordMap.get(next));
        } else {
          wordMap.put(next, 1);
        }
      }
    }
  }

  /**
   * Define cleanup process and emit k,v pairs form hash map.
   */
  protected void cleanup(Context context) throws IOException, InterruptedException {
    /* After aggregation of hash map emmit every key pair */
    for (Entry<String, Integer> ent : wordMap.entrySet()) {
      context.write(new Text(ent.getKey()), new IntWritable(ent.getValue()));
    }
  }
}