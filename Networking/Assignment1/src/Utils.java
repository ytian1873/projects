import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Utils {

    public static final int SERVER_PORT = 8080;

    public static final int INT_BYTES = 2;
    public static final int MAX_BYTES_PER_BUFFER = 16;

    /**
     * Convert an integer into byte array, using big endian
     *
     * @param num number to convert
     * @return byte array of corresponding number
     */
    public static byte[] intToBytes(short num) {
        ByteBuffer bb = ByteBuffer.allocate(INT_BYTES);  // Default by Big Endian
        bb.putShort(num);
        return bb.array();
    }

    /**
     * Write a single expression into socket output stream
     *
     * @param outStream the output stream
     * @param msg       the byte array form of expression to write
     * @throws IOException
     */
    public static void encodeSingleMessage(OutputStream outStream, byte[] msg) throws IOException {
        outStream.write(Utils.intToBytes((short) 1));
        outStream.flush();

        outStream.write(Utils.intToBytes((short) msg.length));
        outStream.flush();

        int i = 0;
        while (i < msg.length) {
            short writeLength = (short) Math.min(MAX_BYTES_PER_BUFFER, msg.length - i);
            outStream.write(msg, i, writeLength);
            i += writeLength;
            outStream.flush();
        }
    }

    /**
     * Encode and write a list of expressions to socket output stream
     *
     * @param outStream the output stream
     * @param message   list of expressions
     * @throws IOException
     */
    public static void encodeMessage(OutputStream outStream, ArrayList<String> message)
            throws IOException {
        short count = (short) message.size();
        outStream.write(Utils.intToBytes(count));
        outStream.flush();

        while (count != 0) {
            for (String msg : message) {
                int i = 0;
                byte[] msgBy = msg.getBytes(StandardCharsets.UTF_8);
                outStream.write(Utils.intToBytes((short) msgBy.length));
                outStream.flush();
                while (i < msgBy.length) {
                    short writeLength = (short) Math.min(MAX_BYTES_PER_BUFFER, msgBy.length - i);
                    outStream.write(msgBy, i, writeLength);
                    i += writeLength;
                    outStream.flush();
                }
            }

            count -= 1;
        }
    }

    /**
     * Convert a byte array into an integer
     *
     * @param byteArr the byte array to be converted
     * @return the integer
     */
    public static short byteToInt(byte[] byteArr) {
        ByteBuffer bb = ByteBuffer.wrap(byteArr); // big-endian by default
        return bb.getShort();
    }

    /**
     * Decode the message from socket into a list of expressions
     *
     * @param stream the input stream
     * @return the list of expressions
     * @throws IOException
     */
    public static ArrayList<String> decodeMessage(InputStream stream) throws IOException {
        ArrayList<String> arrList = new ArrayList<>();

        byte[] countArr = new byte[INT_BYTES];
//        stream.read(countArr);
        stream.read(countArr, 0, INT_BYTES);
        int count = byteToInt(countArr);

        while (count != 0) {
            StringBuilder strBuilder = new StringBuilder();

            byte[] len = new byte[INT_BYTES];
//            stream.read(len);
            stream.read(len, 0, INT_BYTES);
            int mesLen = byteToInt(len);
            while (mesLen > 0) {
                int readByte = Math.min(mesLen, MAX_BYTES_PER_BUFFER);
                byte[] byteMsg = new byte[readByte];
//                stream.read(byteMsg);
                stream.read(byteMsg, 0, readByte);
                String msg = new String(byteMsg, StandardCharsets.UTF_8);
                strBuilder.append(msg);
                mesLen -= readByte;
            }

            arrList.add(strBuilder.toString());
            count -= 1;
        }

        return arrList;
    }

    /**
     * Do the calculation for a single expression
     *
     * @param expression the expression to be calculated
     * @return string format of the result of expression
     * @throws IOException
     */
    public static String calculateSum(String expression) throws IOException {
        int cur = 0;
        int sum = 0;
        char sign = '+';

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch != '+' && ch != '-') {
                cur = cur * 10 + (ch - '0');
            }

            if (ch == '+' || ch == '-' || i == expression.length() - 1) {
                if (sign == '-') {
                    cur = cur * (-1);
                }
                sum += cur;
                cur = 0;
                sign = ch;
            }
        }

        return String.valueOf(sum);
    }

    /**
     * Send the response to client
     *
     * @param stream   the output stream
     * @param messages the list of expressions
     * @throws IOException
     */
    public static void sendResults(OutputStream stream, ArrayList<String> messages) throws IOException {
        ArrayList<String> res = new ArrayList<>();
        for (String mes : messages) {
            res.add(calculateSum(mes));
        }
        encodeMessage(stream, res);
    }
}
