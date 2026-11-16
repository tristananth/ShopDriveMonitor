# Sistem Monitoring Servis Berkala Mobil – Shop & Drive Cempaka Putih

Aplikasi Java Swing (GUI) untuk memantau jadwal servis berkala mobil pelanggan.
Tugas Besar Dasar Pemrograman Java – Tema 3 (Kelompok 9).

## Menjalankan
Prasyarat: JDK 11 atau lebih baru.

- Linux/macOS: `./build.sh` lalu `./run.sh`
- Windows: `build.bat` lalu `run.bat`
- Mode teks: `./run.sh --console`
- Simulasi tanggal (demo notifikasi): `./run.sh --tanggal=2026-11-16`

Data tersimpan di folder `data/` (CSV, pemisah `;`). Data contoh bersifat fiktif.

## Fitur
1. Input jarak tempuh (odometer) & tanggal servis terakhir tiap kendaraan.
2. Notifikasi servis otomatis (popup saat dibuka, daftar di dashboard, tombol Notifikasi).
3. Status: Aman / Segera Servis (sisa ≤ 500 km atau ≤ 14 hari) / Terlambat (batas km atau tanggal terlewati).
4. Riwayat servis + total biaya, proyeksi jadwal servis, checklist pekerjaan per kelipatan km.
5. Ekspor pengingat pelanggan (.txt) dan backup data (.csv).

## Struktur
`shopdrive.model` (data) · `shopdrive.service` (logika) · `shopdrive.data` (file/stream) · `shopdrive.ui` (Swing) · `shopdrive.tools` (uji & tangkapan layar).
Uji fungsional: `java -cp out shopdrive.tools.UjiFungsi`
