# Naze Launcher — Spesifikasi Produk v1.0

**Status:** Disetujui (draft → baseline)
**Source of truth desain:** mockup HTML "Naze Launcher" (5 Okt 2026)
**Repositorium:** Ndolzz/naze-launcher (Android, Jetpack Compose, Kotlin)

> Dokumen ini adalah acuan pengembangan. Setiap commit implementasi harus
> mengacu nomor bagian spec yang dipenuhinya (mis. `feat(4.3): ...`).
> Perubahan terhadap spec harus melalui edit dokumen ini, bukan keputusan obrolan.

## 1. Visi

Naze Launcher adalah launcher Android bergaya **"premium OS"**: gelap, tenang,
tipografi kuat, gradasi biru-ungu-pink sebagai satu-satunya aksen. Semua data
yang ditampilkan **nyata** (jam, baterai, cuaca, aplikasi terinstal) — bukan
data contoh dari mockup.

## 2. Fondasi Visual (WAJIB di semua layar)

| Elemen | Nilai |
|---|---|
| Latar dasar | Vertikal #0B1020 (ink) → #050510 (night) |
| Glow bawah | Indigo #141A44 kiri-bawah + violet #2A1A5E kanan-bawah |
| Grid halus | Garis 1px opasitas ±5%, jarak ±34dp |
| Teks utama | #F4F6FF (off-white) |
| Teks sekunder | #B8C4FF (mist) |
| Aksen / gradasi | #4F7CFF → #8B5CF6 → #EC4899 (135°) |
| Garis batas | #252D5C |
| Font tampilan | Space Grotesk (angka jam Bold, sisanya Light–Medium) |
| Font monospace | JetBrains Mono (status bar, label kecil) |

Kriteria terima: tidak ada layar dengan warna di luar palet ini; gradasi hanya
pada menit jam, handle, tombol aktif, dan garis penanda.

## 3. Non-Goals (di luar cakupan v1.0)

- Wallpaper/tema sesuai cuaca — DIHAPUS. Palet konstan. (Keputusan pengguna,
  5 Okt 2026: identitas visual konsisten.)
- Tema terang / auto light-dark.
- Widget musik & agenda dengan data nyata di v1.0 (visual mengikuti mockup;
  sumber data nyata = tugas v1.1).
- Wallpaper bergerak (stars / hujan kode) — statis dulu demi performa.

## 4. Layar & Kriteria Terima

### 4.1 Home
- Jam bertumpuk: baris jam (putih) + baris menit (bergradasi), Space Grotesk
  Bold, auto-shrink agar tidak overflow.
- Hari (Bold) + tanggal di samping jam; di bawahnya suhu & kondisi cuaca NYATA
  (OpenWeatherMap, cache offline, status jujur saat gagal).
- Baris "berikutnya": agenda terdekat; disembunyikan bila tidak ada.
- Grid ikon 4 kolom + dock 4 aplikasi dari aplikasi terinstal asli;
  long-press = pin/unpin/info aplikasi.
- Gestur: swipe atas = drawer; ketuk-dua-kali = matikan layar (opsional).
- Terima jika: menit bergradasi; ikon dari aplikasi nyata; tanpa crash 10 menit.

### 4.2 App Drawer
- Buka via swipe atas / handle bawah. Latar night, pencarian di atas, daftar
  per kategori.
- Pencarian real-time; state kosong jelas.
- Terima jika: semua aplikasi launchable muncul dan bisa dibuka.

### 4.3 Panel Atas (QS)
- Tarik dari atas: jam + tanggal, tile bulat (Wi-Fi, Bluetooth, senter, mode
  pesawat, DND, rotasi, hemat baterai, lokasi); tile aktif = gradasi.
- Slider kecerahan NYATA (mengubah Settings.System.screenBrightness).
- Notifikasi NYATA via NotificationListenerService; "Hapus semua" berfungsi.
- Terima jika: senter & kecerahan benar-benar mengubah perangkat.

### 4.4 Lockscreen
- Jam besar bertumpuk, 3 gaya: tumpuk / analog / terminal.
- Dua tombol bulat: senter (aktif = gradasi) & kamera — berfungsi nyata.
- PIN 4 digit (default 1234, dapat diganti); salah = animasi shake + pesan.
- Notifikasi terkunci terbaca setelah PIN.
- Terima jika: senter lockscreen menyalakan torch perangkat.

### 4.5 Settings
- Tema warna (10 preset), ukuran ikon (Kecil/Sedang/Besar), wallpaper statis,
  gaya jam, toggle: ketuk-dua-kali, widget, nama ikon, PIN.
- "Kembalikan bawaan" mereset semua preferensi.
- Tanpa toggle tema cuaca/suhu/waktu (dihapus dari produk).
- Terima jika: setiap perubahan langsung terlihat dan persisten setelah restart.

### 4.6 Ruang Pribadi (Vault)
- Kode akses 4-8 karakter (angka/*), ganti kode, kunci vault.
- Aplikasi tersembunyi tidak tampil di home & drawer; kode salah = shake.
- Terima jika: menyembunyikan aplikasi nyata dari drawer.

### 4.7 Widget (grid 2 kolom, dapat digeser)
- Cuaca+agenda, musik (visual mockup), baterai (cincin % nyata + estimasi),
  catatan cepat (tersimpan lokal).
- Toggle tampil/sembunyi di settings.

## 5. Kebijakan Data Nyata

| Data | Sumber |
|---|---|
| Jam/tanggal | Sistem perangkat |
| Baterai | BatteryManager |
| Senter | CameraManager torch |
| Kecerahan | Settings.System |
| Cuaca | OpenWeatherMap (kunci pengguna, cache terakhir) |
| Aplikasi | PackageManager |
| Notifikasi | NotificationListenerService |
| Lokasi | FusedLocation (izin opsional) |

## 6. Definition of Done (per rilis)

1. CI GitHub Actions hijau; artifact naze-launcher-debug-apk terbit.
2. Alur tanpa crash di perangkat: boot → home → drawer → buka app → lock →
   unlock → panel atas → settings.
3. Tidak ada refleksi ke internal library (pelajaran NoSuchFieldException).
4. Semua layar memenuhi kriteria terima bagian 4.
5. Commit mengacu nomor bagian spec.

## 7. Status Implementasi vs Spec

| Bagian | Status |
|---|---|
| Palet & latar (glow+grid) | Selesai — commit e73a9ad |
| Jam bertumpuk bergradasi | Selesai — commit e73a9ad |
| Hapus tema cuaca dari settings | Selesai — commit 803c3ee |
| Drawer, dock, search | Ada — perlu audit terhadap 4.1-4.2 |
| Panel atas (QS+notifikasi) | Belum ada |
| Lockscreen PIN+senter | Ada struktur — perlu audit terhadap 4.4 |
| Ruang pribadi | Ada struktur — perlu audit terhadap 4.6 |
| Widget (baterai nyata, catatan) | Belum ada |
| Jam analog & terminal | Belum ada |

## 8. Urutan Pengerjaan Berikutnya

1. Audit drawer/dock/lockscreen/vault terhadap spec; tutup gap.
2. Panel atas (QS + kecerahan + notifikasi nyata).
3. Widget home (baterai nyata, catatan, cuaca).
4. Gaya jam analog & terminal.
5. Audit akhir DoD + rilis APK.
