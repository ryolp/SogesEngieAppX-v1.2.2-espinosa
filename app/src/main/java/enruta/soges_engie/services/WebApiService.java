package enruta.soges_engie.services;

import enruta.soges_engie.BuildConfig;
import enruta.soges_engie.interfaces.IWebApi;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebApiService {
    private static Retrofit _retrofit;
    private static Retrofit.Builder _builder;
    private static HttpLoggingInterceptor _loggingInterceptor;
    private static OkHttpClient.Builder _httpClient;

    public static IWebApi Create(){

        //apiURL = "http://192.168.2.123:8182/";
        String apiURL = BuildConfig.BASE_URL;

        return Create(apiURL);
    }

    public static IWebApi Create(String apiURL) {
        try {
            if (!apiURL.endsWith("/"))
                apiURL = apiURL + "/";

            _builder = new Retrofit.Builder()
                    .baseUrl(apiURL)
                    .addConverterFactory(GsonConverterFactory.create());

            _retrofit = _builder.build();

            _loggingInterceptor =
                    new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BASIC);

            _httpClient = new OkHttpClient.Builder();

            if (!_httpClient.interceptors().contains(_loggingInterceptor)) {
                _httpClient.addInterceptor(_loggingInterceptor);
                _builder.client(_httpClient.build());
            }

            return _retrofit.create(IWebApi.class);
        } catch (Exception e) {
            return null;
        }
    }
}
