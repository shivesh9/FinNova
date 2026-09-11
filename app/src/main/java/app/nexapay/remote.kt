package app.nexapay

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/** ExchangeRate-API client interface for real-time currency conversion. */
interface ExchangeRateApi {
    @GET("latest/{base}")
    suspend fun latestFree(@Path("base") base: String): ExchangeRateResponseDto

    @GET("{apiKey}/pair/{from}/{to}/{amount}")
    suspend fun convertPairWithKey(
        @Path("apiKey") apiKey: String,
        @Path("from") from: String,
        @Path("to") to: String,
        @Path("amount") amount: Double
    ): ExchangeRatePairResponseDto
}

data class ExchangeRateResponseDto(
    val result: String?,
    val provider: String?,
    @SerializedName("time_last_update_utc") val timeLastUpdateUtc: String?,
    @SerializedName("base_code") val baseCode: String?,
    val rates: Map<String, Double>?
)

data class ExchangeRatePairResponseDto(
    val result: String?,
    @SerializedName("documentation") val documentation: String?,
    @SerializedName("terms_of_use") val termsOfUse: String?,
    @SerializedName("time_last_update_unix") val timeLastUpdateUnix: Long?,
    @SerializedName("time_last_update_utc") val timeLastUpdateUtc: String?,
    @SerializedName("base_code") val baseCode: String?,
    @SerializedName("target_code") val targetCode: String?,
    @SerializedName("conversion_rate") val conversionRate: Double?,
    @SerializedName("conversion_result") val conversionResult: Double?,
    @SerializedName("error-type") val errorType: String?
)

object NetworkClient {
    val freeApi: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/v6/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }

    val keyApi: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://v6.exchangerate-api.com/v6/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}
