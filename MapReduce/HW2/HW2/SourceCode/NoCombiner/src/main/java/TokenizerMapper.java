import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TokenizerMapper
    extends Mapper<Object, Text, Text, IntWritable> {

  private final static IntWritable one = new IntWritable(1);
  private Text word = new Text();

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
        word.set(next);
        context.write(word, one);
      }
    }
  }
}