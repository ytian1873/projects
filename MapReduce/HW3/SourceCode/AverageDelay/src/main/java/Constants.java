import java.util.Calendar;
import java.util.Date;

/**
 * The type Constants.
 */
public class Constants {

  /**
   * The constant VALID_STATUS.
   */
  public static final String VALID_STATUS = "0.00";
  /**
   * The constant VALID_DEP.
   */
  public static final String VALID_DEP = "ORD";
  /**
   * The constant VALID_ARR.
   */
  public static final String VALID_ARR = "JFK";
  /**
   * The constant VALID_DATE_LOWER_BOUND.
   */
  public static final Date VALID_DATE_LOWER_BOUND = new Date(107, Calendar.JUNE,1);
  /**
   * The constant VALID_DATE_UPPER_BOUND.
   */
  public static final Date VALID_DATE_UPPER_BOUND = new Date(108, Calendar.MAY,31);

  /**
   * The constant DATE_IND.
   */
  public static final int DATE_IND = 5;
  /**
   * The constant DEP_TIME_IND.
   */
  public static final int DEP_TIME_IND = 24;
  /**
   * The constant ARR_TIME_IND.
   */
  public static final int ARR_TIME_IND = 35;
  /**
   * The constant DEP_IND.
   */
  public static final int DEP_IND = 11;
  /**
   * The constant ARR_IND.
   */
  public static final int ARR_IND =17;
  /**
   * The constant DELAY_IND.
   */
  public static final int DELAY_IND = 37;
  /**
   * The constant CANCELLED_IND.
   */
  public static final int CANCELLED_IND = 41;
  /**
   * The constant DIVERTED_IND.
   */
  public static final int DIVERTED_IND = 43;
}
