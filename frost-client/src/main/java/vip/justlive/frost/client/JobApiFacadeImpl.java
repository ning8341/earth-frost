package vip.justlive.frost.client;

import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Route;
import vip.justlive.frost.api.facade.JobApiFacade;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * job api facade 实现类
 * 
 * @author wubo
 *
 */
public class JobApiFacadeImpl implements JobApiFacade {

  public static final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");

  private OkHttpClient client;
  private Gson gson = new Gson();
  private ClientProperties clientProps;

  public JobApiFacadeImpl(ClientProperties clientProps) {
    this.clientProps = clientProps;
    this.init();
  }

  void init() {
    client = new OkHttpClient.Builder().authenticator(new Authenticator() {
      @Override
      public Request authenticate(Route route, okhttp3.Response response) throws IOException {
        String credential = Credentials.basic(clientProps.getUsername(), clientProps.getPassword());
        return response.request().newBuilder().header("Authorization", credential).build();
      }
    }).build();
  }

  <T> T postJson(String url, Object obj, Class<T> clazz) {
    String json = gson.toJson(obj);
    RequestBody body = RequestBody.create(MEDIA_JSON, json);
    Request request = new Request.Builder().url(url).post(body).build();
    return handle(request, clazz);
  }

  <T> T handle(Request request, Class<T> clazz) {
    try {
      okhttp3.Response resp = client.newCall(request).execute();
      String respBody = resp.body().string();
      JsonElement element = new JsonParser().parse(respBody);
      JsonObject data = element.getAsJsonObject();
      if (data.get(Constants.RESP_IS_SUCCESS).getAsBoolean()) {
        return gson.fromJson(data.get("data"), clazz);
      } else {
        throw Exceptions.fail(data.get(Constants.RESP_CODE_FIELD).getAsString(),
            data.get(Constants.RESP_MESSAGE_FIELD).getAsString());
      }
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }


  @Override
  public String addJob(JobInfo jobInfo) {
    return postJson(clientProps.getBaseUrl() + "/addJob", jobInfo, String.class);
  }

  @Override
  public void updateJob(JobInfo jobInfo) {
    postJson(clientProps.getBaseUrl() + "/updateJob", jobInfo, String.class);
  }

  @Override
  public void pauseJob(String jobId) {
    FormBody body = new FormBody.Builder().add("id", jobId).build();
    Request request =
        new Request.Builder().url(clientProps.getBaseUrl() + "/pauseJob").post(body).build();
    handle(request, String.class);
  }

  @Override
  public void resumeJob(String jobId) {
    FormBody body = new FormBody.Builder().add("id", jobId).build();
    Request request =
        new Request.Builder().url(clientProps.getBaseUrl() + "/resumeJob").post(body).build();
    handle(request, String.class);
  }

  @Override
  public void removeJob(String jobId) {
    FormBody body = new FormBody.Builder().add("id", jobId).build();
    Request request =
        new Request.Builder().url(clientProps.getBaseUrl() + "/removeJob").post(body).build();
    handle(request, String.class);
  }

  @Override
  public void triggerJob(String jobId) {
    FormBody body = new FormBody.Builder().add("id", jobId).build();
    Request request =
        new Request.Builder().url(clientProps.getBaseUrl() + "/triggerJob").post(body).build();
    handle(request, String.class);
  }

}
