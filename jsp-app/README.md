# KasKu JSP

Aplikasi pengelolaan keuangan pribadi untuk tugas akhir Pemrograman Web.

**Pengerjaan kelompok (2 anggota).**

| Anggota | NRP | Kontribusi |
|---|---|---|
| Muhammad Elmiro Rayhan | 5025231296 | 75% |
| Ahmad Faiz Tajuddin | 5025231291 | 25% |

Elmiro menangani analisis fitur, database, dan pengembangan aplikasi. Faiz menangani tampilan, membantu pengujian, serta menyiapkan screenshot dan dokumentasi. Deskripsi tugas lengkap ada di laporan berbahasa Inggris sederhana: `docs/LAPORAN.md` dan `docs/Laporan_KasKu_5025231296.pdf`.

Versi ini meneruskan gagasan KasKu dari repository https://github.com/truewxrld/kasku dengan implementasi **Java, Jakarta Servlet, JSP, dan JDBC**. Aplikasi Laravel lama tidak diperlukan untuk menjalankan folder ini. Data Laravel tidak dimigrasikan otomatis.

## Menjalankan di Windows

1. Pasang **JDK 17 atau 21** dan **Apache Maven 3.9+**, lalu buka kembali terminal VS Code.
2. Ekstrak ZIP. Buka folder `kasku-jsp` yang berisi `pom.xml`.
3. Jalankan:

```powershell
java -version
mvn -version
mvn clean package
.\run.bat
```

4. Buka **http://localhost:8080**. Klik **Jelajahi akun demo** untuk melihat data contoh, atau **Daftar gratis** untuk membuat akun kosong.
5. Hentikan server dengan `Ctrl+C`.

Tanpa PostgreSQL, aplikasi otomatis menggunakan H2 berbasis file di `data/kasku.mv.db`. Data akun normal tetap ada setelah server dimatikan. Folder `data` jangan dihapus jika masih dibutuhkan. Tidak ada akun admin atau kata sandi bawaan. Setiap sesi demo membuat akun contoh tersendiri.

Jika port 8080 dipakai aplikasi lain:

```powershell
$env:PORT = "8081"
.\run.bat
```

Lalu buka http://localhost:8081. Jika source Java berubah, jalankan `mvn clean package` lagi sebelum menjalankan aplikasi.

## Menjalankan di Linux atau macOS

```bash
mvn clean package
sh run.sh
```

## Delapan modul CRUD

| Modul | Create | Read dan detail | Update | Delete | Tabel |
|---|---|---|---|---|---|
| Dompet | Ya | Ya | Ya | Ya, jika belum dipakai | wallets |
| Kategori | Ya | Ya | Ya | Ya, jika belum dipakai | categories |
| Transaksi | Ya | Ya | Ya | Ya | transactions |
| Anggaran | Ya | Ya | Ya | Ya | budgets |
| Target tabungan | Ya | Ya | Ya | Ya | goals |
| Tagihan | Ya | Ya | Ya | Ya | bills |
| Utang dan piutang | Ya | Ya | Ya | Ya | debts |
| Catatan keuangan | Ya | Ya | Ya | Ya | notes |

Tambahan: registrasi/login/logout, dashboard, grafik enam bulan, pencarian, filter jenis, paginasi, detail data, konfirmasi hapus, laporan per rentang tanggal, dan ekspor CSV.

## Aturan keuangan

- Saldo dompet = saldo awal + transaksi kategori pemasukan - transaksi kategori pengeluaran. Nominal memakai `BigDecimal`/`NUMERIC(15,2)`.
- Semua transaksi yang dicatat termasuk dalam saldo. Gunakan tanggal transaksi aktual; tanggal jatuh tempo tersedia pada tagihan/utang.
- Jenis kategori yang sudah dipakai transaksi atau anggaran tidak dapat diubah.
- Satu kategori pengeluaran hanya boleh memiliki satu anggaran pada bulan yang sama.
- Anggaran terpakai dihitung dari transaksi kategori dan bulan terkait; tidak mengubah saldo.
- Dana target tabungan, pelunasan tagihan, dan pembayaran utang adalah catatan manual. Jika pembayaran perlu memengaruhi saldo, catat transaksi juga.
- Hapus dompet/kategori ditolak jika masih direferensikan transaksi/anggaran.
- Seluruh modul difilter dengan `user_id`. Foreign key gabungan memblokir relasi lintas pemilik.

## Database PostgreSQL

Schema otomatis dibuat saat pertama kali server berjalan. SQL yang sama tersedia di `database/schema.sql` untuk pemeriksaan atau eksekusi manual. Tidak ada operasi DROP pada startup.

Contoh koneksi PostgreSQL lokal, menggunakan akun/database yang dibuat sendiri:

```powershell
$env:JDBC_DATABASE_URL = "jdbc:postgresql://localhost:5432/kasku"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "kata-sandi-postgres-lokal"
.\run.bat
```

Nilai di atas adalah contoh. Simpan kata sandi asli hanya di environment. Jangan commit `.env` atau data akun.

Untuk hosting, gunakan `DATABASE_URL` dari PostgreSQL. URL diawali `postgresql://` atau `postgres://`. Koneksi ini memakai SSL jika URL tidak menetapkan parameter lain. `APP_ENV=production` menolak fallback ke H2 agar data tidak hilang akibat filesystem hosting sementara.

| Environment | Keterangan |
|---|---|
| PORT | Port HTTP, default 8080 |
| DATABASE_URL | URL PostgreSQL, diprioritaskan untuk hosting |
| JDBC_DATABASE_URL | URL JDBC alternatif untuk PostgreSQL/H2 |
| DB_USER / DB_PASSWORD | Kredensial untuk JDBC_DATABASE_URL |
| APP_ENV | Set `production` saat deploy |
| COOKIE_SECURE | `true` pada hosting HTTPS; `false` untuk localhost HTTP |
| DEMO_ENABLED | `true` default; set `false` untuk mematikan tombol demo |

## Pengujian

```bash
mvn test
```

Tujuh tes pada `FinanceTest` memeriksa CRUD delapan modul, perhitungan saldo setelah edit/hapus, foreign key lintas pengguna, validasi nominal, anggaran bulanan, hashing kata sandi, CSV, dan laporan kosong. Suite HTTP opsional membutuhkan Python 3:

```bash
mvn -Dkasku.http=true test
```

Di Windows jika perintah Python adalah `python`, set `$env:TEST_PYTHON = "python"` dahulu. Suite HTTP memakai database in-memory tersendiri dan membuat akun uji sementara. Alternatif saat server sudah berjalan: `python scripts/smoke_test.py http://localhost:8080`.

Hasil pemeriksaan pengembangan: **7 tes logika lulus dan 90 assertion HTTP lulus**, ditambah pemeriksaan browser desktop. Bukti berada di `docs/unit-test-results.txt`, `docs/http-suite-results.txt`, dan `docs/http-test-results.json`. Pengujian menggunakan H2 mode PostgreSQL; PostgreSQL server dan deployment publik belum dijalankan. CSS memiliki breakpoint perangkat kecil; uji pada HP nyata sebelum pengumpulan.

## Build WAR dan Docker

`mvn clean package` menghasilkan `target/kasku.war` yang dapat dipasang ke **Tomcat 10.1**. Jangan memakai Tomcat 9 karena namespace servlet berbeda. Jika WAR dinamai `kasku.war`, alamatnya menjadi `/kasku` dan semua tautan mengikuti context path.

```bash
docker build -t kasku-jsp .
docker run --rm -p 8080:8080 -v kasku-data:/app/data kasku-jsp
```

Dockerfile menggunakan JDK 17 saat build dan JRE 17 saat runtime. Build image Docker belum dijalankan di lingkungan pengembangan ini; Java/JSP dan WAR sudah dibangun dan diuji di luar Docker.

## Upload GitHub dan deploy

Baca `docs/PANDUAN_DEPLOY.md`. Push dan deployment dilakukan oleh pemilik proyek; paket ini belum dipublikasikan. `render.yaml` adalah konfigurasi siap digunakan, bukan bukti website sudah online.

## Struktur penting

- `src/main/java/id/kasku`: servlet, JDBC, validasi, perhitungan, autentikasi, dan data demo.
- `src/main/webapp/WEB-INF/views`: JSP yang tidak dapat diakses langsung tanpa controller.
- `src/main/webapp/assets`: CSS, JavaScript, ikon SVG, dan font Manrope lokal.
- `src/main/resources/schema.sql`: sumber schema yang dipakai saat startup.
- `database`: salinan DDL dan diagram CDM/PDM.
- `docs`: laporan, screenshot asli aplikasi, panduan, dan bukti tes.
- `release/kasku.war`: WAR hasil build yang disertakan pada paket unduhan.

Font Manrope menggunakan SIL Open Font License; lisensi disertakan di `assets/fonts/OFL.txt`. Diagram dan data demo dibuat untuk proyek ini. Bantuan AI digunakan pada penyusunan kode dan draf dokumentasi; pemilik proyek perlu memahami, meninjau, dan menguji kembali sebelum pengumpulan.
