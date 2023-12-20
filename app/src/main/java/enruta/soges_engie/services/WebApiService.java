package enruta.soges_engie.services;

import java.util.concurrent.TimeUnit;

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
    private static OkHttpClient.Builder _okHttpClientBuilder;
    private static OkHttpClient _okHttpClient;

    public static IWebApi Create(){

        //apiURL = "http://192.168.2.123:8182/";
        String apiURL = BuildConfig.BASE_URL;

        return Create(apiURL);
    }

    public static IWebApi Create(String apiURL) {
        try {
            if (!apiURL.endsWith("/"))
                apiURL = apiURL + "/";

//            _loggingInterceptor =
//                    new HttpLoggingInterceptor()
//                            .setLevel(HttpLoggingInterceptor.Level.BASIC);

//            _loggingInterceptor =
//                    new HttpLoggingInterceptor()
//                            .setLevel(HttpLoggingInterceptor.Level.BODY);

            _okHttpClientBuilder = new OkHttpClient.Builder();

//            if (!_okHttpClientBuilder.interceptors().contains(_loggingInterceptor)) {
//                _okHttpClientBuilder.addInterceptor(_loggingInterceptor);
//            }

            _okHttpClient = _okHttpClientBuilder
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

//            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .connectTimeout(60, TimeUnit.SECONDS)
//                    .writeTimeout(5, TimeUnit.MINUTES)
//                    .readTimeout(60, TimeUnit.SECONDS)
//                    .build();

            _builder = new Retrofit.Builder()
                    .baseUrl(apiURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(_okHttpClient);

            _retrofit = _builder.build();

            return _retrofit.create(IWebApi.class);
        } catch (Exception e) {
            return null;
        }
    }
}
