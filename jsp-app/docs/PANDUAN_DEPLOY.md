# Panduan GitHub dan hosting KasKu JSP

Anggota kelompok: **Muhammad Elmiro Rayhan, 5025231296 (75%)** dan **Ahmad Faiz Tajuddin, 5025231291 (25%)**. Upload dilakukan sendiri oleh pemilik repository sesuai permintaan. Tautan repository awal: https://github.com/truewxrld/kasku.

## 1 Menjalankan dan memeriksa lokal

Pasang JDK 17/21 dan Maven 3.9+, buka folder yang berisi `pom.xml`, jalankan `mvn clean package`, lalu `run.bat` di Windows atau `sh run.sh` di Linux. Buka http://localhost:8080 dan coba akun demo. Buat akun sendiri untuk data yang ingin dipertahankan.

## 2 Menggabungkan ke repository lama

Repository lama memakai Laravel. Cara yang disarankan adalah menambahkan versi JSP dalam folder `jsp-app` sehingga versi lama tetap tersimpan.

```powershell
git clone https://github.com/truewxrld/kasku.git
cd kasku
git switch -c final-jsp
```

Jika sudah memiliki clone lokal, cukup buka folder tersebut lalu buat branch. **Salin seluruh isi folder `kasku-jsp` hasil ekstraksi ke folder `jsp-app` di dalam clone**. Pastikan hasilnya `kasku/jsp-app/pom.xml`, bukan dua lapis folder `kasku/jsp-app/kasku-jsp/pom.xml`.

```powershell
git status
git add jsp-app
git commit -m "Add KasKu JSP final project with eight CRUD modules"
git push -u origin final-jsp
```

Masuk ke GitHub saat diminta. Periksa file di branch `final-jsp`. Jika perlu, buat pull request ke `main`. Jangan menghapus versi Laravel sebelum memastikan cadangannya tersimpan. Jika menggunakan repository baru yang khusus JSP, letakkan seluruh isi proyek langsung pada root repository.

## 3 Membuat database di Render

1. Buka https://dashboard.render.com dan masuk dengan akun sendiri.
2. Buat PostgreSQL baru, nama `kasku-db`, database `kasku`, pilih region yang sama dengan web service (misalnya Singapore) dan paket **Free** jika tersedia.
3. Setelah tersedia, salin **Internal Database URL** untuk dipakai pada environment web service di region yang sama. Jangan memasukkan URL beserta kata sandinya ke source code, screenshot publik, atau laporan.

## 4 Membuat web service Docker

1. Pilih New → Web Service, hubungkan repository `truewxrld/kasku` dan branch `final-jsp`.
2. Isi nama `kasku-elmiro` atau nama unik lain yang masih tersedia.
3. Runtime: **Docker**. Root Directory: **jsp-app**. Dockerfile Path: **./Dockerfile**. Docker Build Context Directory: **.** relatif root directory. Jika repository berisi JSP langsung di root, biarkan Root Directory kosong.
4. Pilih **Free**, region sama dengan database, dan tambahkan environment berikut.

| Key | Value |
|---|---|
| DATABASE_URL | Internal Database URL dari PostgreSQL |
| APP_ENV | production |
| COOKIE_SECURE | true |
| DEMO_ENABLED | true untuk demonstrasi; false untuk penggunaan pribadi |

5. Gunakan health check path `/health`.
6. Deploy dan tunggu log menunjukkan aplikasi siap. Server membaca `PORT` yang diberikan Render.
7. Buka URL yang benar-benar muncul di dashboard Render. Coba daftar, masuk, tambah data, edit, dan hapus. Buka kembali setelah restart untuk memeriksa data PostgreSQL tetap tersedia.

**Alternatif Blueprint:** `render.yaml` dalam proyek membuat satu web service dan satu PostgreSQL. Konfigurasi ini mengasumsikan proyek JSP ada di root. Jika menaruhnya pada `jsp-app`, buat salinan `render.yaml` di root repository lalu tambahkan `rootDir: jsp-app` pada service. Gunakan tampilan konfigurasi Render untuk memastikan yang dipilih tetap paket gratis sebelum deploy.

## 5 Batas hosting gratis

Berdasarkan dokumentasi resmi yang diperiksa pada 18 November 2026, web service gratis Render tidur setelah 15 menit tanpa trafik dan PostgreSQL gratis berakhir setelah 30 hari. File lokal web service bersifat sementara, sehingga jangan mengandalkan H2 file untuk hosting. Simpan data di PostgreSQL, unduh ekspor saat perlu, dan cek masa aktif database menjelang penilaian.

Rujukan: https://render.com/docs/free dan https://render.com/docs/monorepo-support. Paket atau label UI dapat berubah; cek kembali sebelum memilih layanan.

## 6 Melengkapi pengumpulan

- Repository yang dikumpulkan harus menunjuk branch atau commit berisi versi JSP.
- Tambahkan URL hosting yang sudah diuji ke laporan setelah deploy.
- Sertakan nama **Muhammad Elmiro Rayhan**, NRP **5025231296**, kontribusi **75%**, dan **Ahmad Faiz Tajuddin**, NRP **5025231291**, kontribusi **25%**.
- Laporan kelompok berbahasa Inggris sederhana tersedia di `docs/Laporan_KasKu_5025231296.pdf`. Teks yang bisa diedit tersedia di `docs/LAPORAN.md`.
- Screenshot di `docs/screenshots` merupakan screenshot aplikasi berjalan dengan data demo, bukan bukti deploy publik.

## Troubleshooting

| Masalah | Tindakan |
|---|---|
| `mvn` tidak dikenali | Pastikan Maven ada di PATH, lalu buka terminal baru. |
| Java terlalu lama | Gunakan JDK 17 atau 21; periksa `java -version` dan `mvn -version`. |
| Port 8080 dipakai | Set environment PORT=8081, lalu gunakan port tersebut pada browser. |
| Database gagal konek | Periksa DATABASE_URL, status PostgreSQL, region, dan masa aktif paket gratis. |
| CSS hilang ketika WAR di Tomcat | Gunakan context path yang sesuai nama WAR dan pastikan seluruh WAR diekstrak. |
| Hapus dompet/kategori ditolak | Hapus transaksi dan anggaran yang masih mereferensikan data tersebut. |
| Tidak bisa masuk localhost setelah setting hosting | Set COOKIE_SECURE=false untuk HTTP localhost dan hapus cookie localhost lama. |
| Data demo tidak terlihat setelah logout | Klik Jelajahi akun demo lagi untuk sesi demo baru. Akun normal dibuat lewat Daftar gratis. |
