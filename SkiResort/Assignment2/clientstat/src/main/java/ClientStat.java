import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.StatisticsApi;
import io.swagger.client.model.APIStats;
import io.swagger.client.model.APIStatsEndpointStats;

public class ClientStat {

  public static void main(String[] args) {
    StatisticsApi statisticsApi = new StatisticsApi();
    ApiClient c = new ApiClient();
    String ADDRESS_OF_SERVER = "http://localhost:8080/server_war_exploded/statistics/";
    c.setBasePath(ADDRESS_OF_SERVER);
    statisticsApi.setApiClient(c);
    ApiResponse<APIStats> res;
    try {
      res = statisticsApi.getPerformanceStatsWithHttpInfo();
      APIStats stats = res.getData();
      APIStatsEndpointStats endpointStats = stats.getEndpointStats().get(0);
      Integer mean = endpointStats.getMean();
      Integer max = endpointStats.getMax();

      System.out.println("===Runtime Statistic Result===");
      System.out.println("Mean: " + mean + "millisecs");
      System.out.println("Max: " + max + "millisecs");
    } catch (ApiException e) {
      e.printStackTrace();
    }
  }
}
