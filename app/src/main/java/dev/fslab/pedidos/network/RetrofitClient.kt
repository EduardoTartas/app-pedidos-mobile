package dev.fslab.pedidos.network


import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient - Singleton que configura e fornece o cliente HTTP
 *
 * Inclui:
 * - AuthInterceptor para injeção automática de Bearer token
 * - TokenAuthenticator para renovação automática em 401
 * - Logging interceptor para debug
 */
object RetrofitClient {

    /**
     * Com `adb reverse tcp:5020 tcp:5020` ativo, o dispositivo físico enxerga o host local via loopback.
     * Ajuste APP_PORT quando necessário.
     */
    const val BASE_URL = "http://127.0.0.1:5020/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .authenticator(TokenAuthenticator())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val usuarioApi: UsuarioApi by lazy {
        retrofit.create(UsuarioApi::class.java)
    }
    val categoriaApi: CategoriaApi by lazy {
        retrofit.create(CategoriaApi::class.java)
    }

    val restauranteApi: RestauranteApi by lazy {
        retrofit.create(RestauranteApi::class.java)
    }

    val enderecoApi: EnderecoApi by lazy {
        retrofit.create(EnderecoApi::class.java)
    }
}
