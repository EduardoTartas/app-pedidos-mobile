package dev.fslab.pedidos.network


import dev.fslab.pedidos.BuildConfig
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
    val BASE_URL: String = BuildConfig.API_BASE_URL

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

    val cardapioApi: CardapioApi by lazy {
        retrofit.create(CardapioApi::class.java)
    }

    val enderecoApi: EnderecoApi by lazy {
        retrofit.create(EnderecoApi::class.java)
    }

    val adicionalApi: AdicionalApi by lazy {
        retrofit.create(AdicionalApi::class.java)
    }

    val pedidoApi: PedidoApi by lazy {
        retrofit.create(PedidoApi::class.java)
    }
}
