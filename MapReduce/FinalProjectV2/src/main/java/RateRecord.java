public class RateRecord {

  private Integer userId;
  private Integer rate;

  public RateRecord() {
    setRate(-1);
    setUserId(-1);
  }

  public RateRecord(Integer userId, Integer rate) {
    this.userId = userId;
    this.rate = rate;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getRate() {
    return rate;
  }

  public void setRate(Integer rate) {
    this.rate = rate;
  }
}
