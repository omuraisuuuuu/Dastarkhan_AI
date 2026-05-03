package com.pm.foodscanner.di

import android.content.Context
import com.pm.foodscanner.BuildConfig
import com.pm.foodscanner.data.local.LocalFoodClassifier
import com.pm.foodscanner.data.remote.HuggingFaceApi
import com.pm.foodscanner.data.remote.RoboflowApi
import com.pm.foodscanner.data.remote.OpenFoodFactsApi
import com.pm.foodscanner.data.repository.AuthRepository
import com.pm.foodscanner.data.repository.BarcodeRepository
import com.pm.foodscanner.data.repository.FoodHistoryRepository
import com.pm.foodscanner.data.repository.MealRepository
import com.pm.foodscanner.data.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthRepository(supabaseClient: SupabaseClient): AuthRepository {
        return AuthRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(supabaseClient: SupabaseClient): ProfileRepository {
        return ProfileRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(httpClient: HttpClient): OpenFoodFactsApi {
        return OpenFoodFactsApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideRoboflowApi(): RoboflowApi {
        return RoboflowApi()
    }

    @Provides
    @Singleton
    fun provideHuggingFaceApi(httpClient: HttpClient): HuggingFaceApi {
        return HuggingFaceApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideBarcodeRepository(
        openFoodFactsApi: OpenFoodFactsApi,
        supabaseClient: SupabaseClient
    ): BarcodeRepository {
        return BarcodeRepository(openFoodFactsApi, supabaseClient)
    }

    @Provides
    @Singleton
    fun provideMealRepository(
        localFoodClassifier: LocalFoodClassifier,
        roboflowApi: RoboflowApi,
        huggingFaceApi: HuggingFaceApi,
        supabaseClient: SupabaseClient
    ): MealRepository {
        return MealRepository(localFoodClassifier, roboflowApi, huggingFaceApi, supabaseClient)
    }

    @Provides
    @Singleton
    fun provideFoodHistoryRepository(supabaseClient: SupabaseClient): FoodHistoryRepository {
        return FoodHistoryRepository(supabaseClient)
    }
}
