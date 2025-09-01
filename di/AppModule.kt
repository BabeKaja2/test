package com.babetech.ucb_admin_access.di

import androidx.room.Room
import com.babetech.ucb_admin_access.api.ApiService
import com.babetech.ucb_admin_access.data.StudentRepository
import com.babetech.ucb_admin_access.data.StudentRepositoryImpl
import com.babetech.ucb_admin_access.data.AttendanceRepository
import com.babetech.ucb_admin_access.data.AttendanceRepositoryImpl
import com.babetech.ucb_admin_access.data.local.AppDatabase
import com.babetech.ucb_admin_access.data.local.MIGRATION_1_2
import com.babetech.ucb_admin_access.data.local.StudentDao
import com.babetech.ucb_admin_access.data.local.AttendanceDao
import com.babetech.ucb_admin_access.ble.BleScanner
import com.babetech.ucb_admin_access.domain.usecase.GetFilteredAttendanceUseCase
import com.babetech.ucb_admin_access.domain.usecase.RecordAttendanceUseCase
import com.babetech.ucb_admin_access.viewmodel.RapportViewModel
import com.babetech.ucb_admin_access.viewmodel.ScannerViewModel
import com.babetech.ucb_admin_access.viewmodel.AttendanceViewModel
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // HTTP client Ktor
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) { level = LogLevel.ALL }
        }
    }

    // ApiService
    single { ApiService(get()) }

    // Room
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "ucb_app_db"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }
    single<StudentDao> { get<AppDatabase>().studentDao() }
    single<AttendanceDao> { get<AppDatabase>().attendanceDao() }

    // Repositories
    single<StudentRepository> {
        StudentRepositoryImpl(get(), get())
    }
    single<AttendanceRepository> {
        AttendanceRepositoryImpl(get())
    }

    // Use Cases
    single { GetFilteredAttendanceUseCase(get()) }
    single { RecordAttendanceUseCase(get()) }

    // BLE Scanner
    single { BleScanner(androidContext()) }

    // ViewModels
    viewModel { ScannerViewModel(get(), get(), get()) }
    viewModel { RapportViewModel(get()) }
    viewModel { AttendanceViewModel(get()) }
}
