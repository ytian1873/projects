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
  private Character[] chars = {'M', 'm', 'N', 'n', 'O', 'o', 'P', 'p', 'Q', 'q'};
  private List<Character> range = new ArrayList<Character>(Arrays.asList(chars));

  public void map(Object key, Text value, Context context
  ) throws IOException, InterruptedException {
    /* To validate strings in the input file, used a char array for qualified range. */
    StringTokenizer itr = new StringTokenizer(value.toString());
    HashMap<String, Integer> wordMap = new HashMap<String, Integer>();

    while (itr.hasMoreTokens()) {
      String next = itr.nextToken();
      Character beginning = next.charAt(0);
      /* For each word, check if the first letter falls into validated range,
       if so send to further work */
      if (range.contains(beginning)) {
        /* Iterate through the value, and aggregate word and count to store the KVP in hash map */
        if (wordMap.containsKey(next)) {
          wordMap.put(next, 1 + wordMap.get(next));
        } else {
          wordMap.put(next, 1);
        }
      }
    }

    /* After aggregation of hash map emmit every key pair */
    for (Entry<String, Integer> ent : wordMap.entrySet()) {
      context.write(new Text(ent.getKey()), new IntWritable(ent.getValue()));
    }
  }
}