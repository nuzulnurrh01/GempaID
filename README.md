# GempaID - Monitor Gempa Indonesia

Aplikasi Android native yang dirancang untuk memantau informasi gempa bumi terkini di wilayah Indonesia secara *real-time* menggunakan data resmi dari BMKG.

## Fitur Utama & Implementasi Teknis
Berikut adalah detail teknis mengenai implementasi fitur dalam proyek ini:

### 1. Desain Material dengan Compose (Materi Pertemuan 9)
Aplikasi ini  dikembangkan menggunakan **Jetpack Compose** dengan standar **Material Design 3**.
*   **Komponen Standar:** Menggunakan `Scaffold`, `LargeTopAppBar`, `Card`, dan `HorizontalDivider`.
*   **Responsif & Rapi:** Menggunakan `nestedScroll` agar TopAppBar mengecil saat daftar di-scroll, memberikan pengalaman pengguna yang modern.
*   **Mode Gelap:** Mendukung tema Dark Mode dan Light Mode yang tersimpan secara persisten.

### 2. Arsitektur MVVM (Materi Pertemuan 10)
Aplikasi mengikuti pola arsitektur **MVVM** untuk pemisahan tanggung jawab yang jelas:
*   **Model:** Representasi data gempa dan pemrosesan logika domain.
*   **View (Compose):** Hanya bertugas menampilkan UI berdasarkan state.
*   **ViewModel:** Mengelola status UI menggunakan `StateFlow` dan menangani logika bisnis seperti pencarian dan pemfilteran data.
*   **Dependency Injection:** Menggunakan **Hilt** untuk manajemen dependensi yang bersih.

### 3. Navigasi Jetpack Compose (Materi Pertemuan 11)
Implementasi navigasi menggunakan **Navigation Compose** dengan pendekatan **Type-Safe Routes**:
*   **Multi-Layar:** Terdiri dari `HomeScreen` (Daftar Gempa) dan `DetailScreen` (Informasi Lengkap).
*   **Pengiriman Argumen:** Data objek Gempa dikirim antar layar secara aman menggunakan serialisasi Kotlin.
*   **NavHost:** Manajemen navigasi terpusat dengan animasi transisi yang halus.

### 4. Menampilkan Data dari Internet (Materi Pertemuan 12)
Aplikasi menarik data secara *online* dari REST API publik **BMKG**:
*   **Retrofit:** Digunakan untuk mengonsumsi endpoint `autogempa.json` dan `gempaterkini.json`.
*   **Asinkronus:** Menggunakan **Kotlin Coroutines** dan **Flow** untuk pengambilan data tanpa memblokir thread UI.
*   **Parsing Data:** Menggunakan **Kotlinx Serialization** untuk mengubah JSON menjadi objek Kotlin secara efisien.

### 5. Menampilkan Gambar dari Internet (Materi Pertemuan 13)
*   **Coil Image Loading:** Menggunakan library **Coil** (`AsyncImage`) untuk memuat gambar peta guncangan (*shakemap*) dari URL BMKG.
*   **Penanganan State:** Dilengkapi dengan *placeholder* saat memuat dan *error fallback* jika gambar gagal diunduh.

### 6. Fitur  (Offline Mode)
*   **Room Database:** Mengimplementasikan caching lokal sehingga data tetap dapat diakses meskipun perangkat sedang *offline*.
*   **Network Monitor:** Deteksi otomatis status internet untuk memberikan peringatan kepada pengguna.

## Informasi Mahasiswa
*   **Nama:** Nuzul Nur Rohman
*   **NIM:** 205410107
*   **Program Studi:** Informatika

## Cara Menjalankan Proyek
1. Clone repositori ini.
2. Buka di Android Studio Ladybug atau versi yang lebih baru.
3. Pastikan perangkat atau emulator Anda memiliki koneksi internet.
4. Klik **Run** untuk menginstal dan menjalankan aplikasi.
