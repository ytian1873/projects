import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * Initiates partitioner, and send words to different partitioner according to their first letters
 */
public class StringPartitioner extends Partitioner<Text, IntWritable> {

  public int getPartition(Text text, IntWritable intWritable, int i) {
//    if (text.charAt(0) == 'M' || text.charAt(0) == 'm') {
//      return 0;
//    } else if (text.charAt(0) == 'N' || text.charAt(0) == 'n') {
//      return 1;
//    } else if (text.charAt(0) == 'O' || text.charAt(0) == 'o') {
//      return 2;
//    } else if (text.charAt(0) == 'P' || text.charAt(0) == 'p') {
//      return 3;
//    } else {
//      return 4;
//    }
    return (text.toString().toLowerCase().charAt(0) + 1) % i;
  }
}
