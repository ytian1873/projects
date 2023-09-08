import java.util.Date;

/**
 * The type Record. Represent a valid, cleaned record of flight data.
 */
public class Record {
  private String middleCity;
  private Date date;
  private Integer time;
  private Double delay;
  private Boolean isFirstFlight;

  /**
   * Instantiates a new Record.
   */
  public Record() {

  }

  /**
   * Instantiates a new Record.
   *
   * @param middleCity the middle city
   * @param date the date
   * @param time the time
   * @param delay the delay
   * @param isFirstFlight the is first flight
   */
  public Record(String middleCity, Date date, Integer time, Double delay,
      Boolean isFirstFlight) {
    this.middleCity = middleCity;
    this.date = date;
    this.time = time;
    this.delay = delay;
    this.isFirstFlight = isFirstFlight;
  }

  /**
   * Gets middle city.
   *
   * @return the middle city
   */
  public String getMiddleCity() {
    return middleCity;
  }

  /**
   * Sets middle city.
   *
   * @param middleCity the middle city
   */
  public void setMiddleCity(String middleCity) {
    this.middleCity = middleCity;
  }

  /**
   * Gets date.
   *
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Sets date.
   *
   * @param date the date
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Gets time.
   *
   * @return the time
   */
  public Integer getTime() {
    return time;
  }

  /**
   * Sets time.
   *
   * @param time the time
   */
  public void setTime(Integer time) {
    this.time = time;
  }

  /**
   * Gets delay.
   *
   * @return the delay
   */
  public Double getDelay() {
    return delay;
  }

  /**
   * Sets delay.
   *
   * @param delay the delay
   */
  public void setDelay(Double delay) {
    this.delay = delay;
  }

  /**
   * Gets first flight.
   *
   * @return the first flight
   */
  public Boolean getFirstFlight() {
    return isFirstFlight;
  }

  /**
   * Sets first flight.
   *
   * @param firstFlight the first flight
   */
  public void setFirstFlight(Boolean firstFlight) {
    isFirstFlight = firstFlight;
  }
}
