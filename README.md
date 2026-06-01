# 📅 WIIS - Why, It Is Sunday?

> *"Ngày mai có học không?"*
> *"Lịch thi ở đâu?"*
> *"Ủa hôm nay học tiết mấy?"*
>
> **WIIS** ra đời để trả lời tất cả những câu hỏi đó trước khi bạn kịp nhắn vào nhóm lớp. 😎

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.x-purple?logo=kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Latest-green?logo=android" />
  <img src="https://img.shields.io/badge/MVVM-Architecture-blue" />
  <img src="https://img.shields.io/badge/Android-SDK%2021+-success?logo=android" />
</p>

---

# 🚀 Giới thiệu

**WIIS (Why, It Is Sunday?)** là ứng dụng Android được xây dựng bằng **Kotlin** và **Jetpack Compose** nhằm hỗ trợ sinh viên quản lý thời gian học tập hiệu quả hơn.

Bởi vì chúng ta đều từng:

* 😴 Ngủ quên và bỏ lỡ tiết học sáng
* 🤯 Quên lịch thi dù đã xem hôm qua
* 📅 Có tới 3 lịch khác nhau nhưng vẫn không biết hôm nay học gì
* ☠️ Nhớ deadline khi deadline đã nhớ mình

WIIS giúp tập trung toàn bộ lịch học, lịch cá nhân và dữ liệu từ hệ thống đào tạo vào một nơi duy nhất.

> **Một ứng dụng lịch dành cho sinh viên được xây bởi chính sinh viên.**

---

# ✨ Tính năng nổi bật

## 📅 Quản lý lịch học

* Hiển thị lịch theo tháng
* Hiển thị lịch theo ngày
* Xem chi tiết từng buổi học
* Quản lý sự kiện cá nhân
* Theo dõi lịch thi và lịch học

💡 Không còn cảnh mở 5 tab rồi vẫn không biết chiều nay học phòng nào.

---

## 🔄 Đồng bộ dữ liệu

* Đồng bộ thời khóa biểu từ hệ thống đào tạo
* Đồng bộ lịch với thiết bị Android
* Đồng bộ dữ liệu người dùng
* Tự động cập nhật dữ liệu

⚡ Một lần đăng nhập, mọi thứ được cập nhật tự động.

---

## 🔔 Thông báo

* Nhắc lịch học
* Nhắc sự kiện
* Nhắc công việc cá nhân
* Thông báo thay đổi lịch

⏰ Để deadline không còn là jumpscare lúc 23:59.

---

## ⚙️ Tùy chỉnh ứng dụng

* Quản lý cài đặt
* Quản lý đồng bộ dữ liệu
* Quản lý phiên đăng nhập

🎛️ Mọi thứ đều nằm trong tầm kiểm soát của bạn.

---

## 🎨 Giao diện hiện đại

* Jetpack Compose
* Material Design 3
* Responsive UI
* Tối ưu trải nghiệm người dùng

✨ Đẹp từ splash screen tới notification.

---

# 🛠 Công nghệ sử dụng

| Thành phần            | Công nghệ                 |
| --------------------- | ------------------------- |
| Ngôn ngữ              | Kotlin                    |
| UI                    | Jetpack Compose           |
| Kiến trúc             | MVVM                      |
| Data Layer            | Repository Pattern        |
| Build Tool            | Gradle Kotlin DSL         |
| Notification          | Android Notification      |
| Calendar              | Android Calendar Provider |
| Async                 | Kotlin Coroutines         |
| State Management      | StateFlow                 |
| Dependency Management | Version Catalog           |

---

# 📱 Yêu cầu hệ thống

| Thành phần     | Phiên bản         |
| -------------- | ----------------- |
| Android Studio | Jellyfish trở lên |
| JDK            | 11+               |
| Android SDK    | 21+               |
| Kotlin         | 2.x               |
| Gradle         | 8.x               |

---

# 📂 Cấu trúc dự án

```text
WIIS/
│
├── app/
│   │
│   ├── calendar/
│   │   ├── CalendarScreen.kt
│   │   └── CalendarVM.kt
│   │
│   ├── data/
│   │   ├── DataScreen.kt
│   │   └── DataVM.kt
│   │
│   ├── setting/
│   │   ├── SettingScreen.kt
│   │   └── SettingVM.kt
│   │
│   ├── splash/
│   │   └── SplashScreen.kt
│   │
│   └── component/
│       ├── BotBar.kt
│       └── nav.kt
│
├── data/
│   │
│   ├── local/
│   │   └── PluginLoader.kt
│   │
│   ├── remote/
│   │   ├── DeviceCalendarClient.kt
│   │   ├── ScheduleApiClient.kt
│   │   └── WebViewLoginHelper.kt
│   │
│   ├── repository/
│   │   └── ScheduleRepository.kt
│   │
│   └── model/
│       ├── AppSetting.kt
│       ├── ScheduleItem.kt
│       ├── SchoolPlugin.kt
│       ├── SyncState.kt
│       └── UserSession.kt
│
├── scheduler/
│   └── NotificationHelper.kt
│
├── ui/
│
├── MainActivity.kt
│
├── AndroidManifest.xml
├── build.gradle.kts
├── proguard-rules.pro
├── README.md
│
└── Gradle Files
```

---

# 🏗 Kiến trúc hệ thống

Ứng dụng áp dụng kiến trúc MVVM:

```text
UI Layer
↓
ViewModel Layer
↓
Repository Layer
↓
Data Source
├── Remote
└── Local
```

---

## 🖥️ UI Layer

```text
app/
├── calendar/
├── data/
├── setting/
└── splash/
```

### Chức năng

* Hiển thị giao diện
* Nhận tương tác người dùng
* Hiển thị trạng thái dữ liệu

🎯 Chỉ tập trung vào trải nghiệm người dùng.

---

## 🧠 ViewModel Layer

```text
CalendarVM.kt
DataVM.kt
SettingVM.kt
```

### Chức năng

* Xử lý logic giao diện
* Quản lý State
* Kết nối Repository
* Đồng bộ dữ liệu với UI

⚡ Nơi mọi dữ liệu được điều phối một cách gọn gàng.

---

## 📦 Repository Layer

```text
data/repository/
└── ScheduleRepository.kt
```

### Chức năng

* Điều phối dữ liệu
* Tách biệt nguồn dữ liệu
* Cung cấp dữ liệu cho ViewModel

🔄 Cầu nối giữa UI và Data Source.

---

## ☁️ Remote Layer

```text
data/remote/
```

### ScheduleApiClient.kt

* Kết nối API
* Đồng bộ thời khóa biểu
* Lấy dữ liệu từ máy chủ

🌐 Kết nối thế giới bên ngoài.

---

### DeviceCalendarClient.kt

* Đọc lịch thiết bị
* Ghi lịch thiết bị
* Đồng bộ lịch Android

📅 Đưa lịch học vào chính chiếc điện thoại của bạn.

---

### WebViewLoginHelper.kt

* Đăng nhập hệ thống trường
* Lấy Cookie
* Lưu Session

🔐 Đăng nhập một lần, sử dụng nhiều lần.

---

## 💾 Local Layer

```text
data/local/
```

### PluginLoader.kt

* Tải plugin
* Quản lý plugin
* Hỗ trợ nhiều hệ thống đào tạo

🧩 Sẵn sàng mở rộng cho nhiều trường học khác nhau.

---

## 📚 Model Layer

```text
data/model/
```

### Các model chính

#### ScheduleItem

Thông tin một lịch học.

#### UserSession

Thông tin phiên đăng nhập.

#### SyncState

Trạng thái đồng bộ dữ liệu.

#### AppSetting

Cấu hình ứng dụng.

#### SchoolPlugin

Thông tin plugin trường học.

---

# 🔔 Notification System

```text
scheduler/
└── NotificationHelper.kt
```

### Hỗ trợ

* Nhắc lịch học
* Nhắc sự kiện
* Nhắc công việc
* Thông báo hệ thống

📢 Vì đôi khi bộ nhớ của điện thoại đáng tin hơn bộ nhớ của sinh viên.

---

# 🔐 Bảo mật

Ứng dụng áp dụng:

* Quản lý Session người dùng
* Lưu trạng thái đăng nhập
* Tách biệt tầng dữ liệu
* Repository Pattern
* Kiểm soát truy cập API

🛡️ Dữ liệu của bạn được xử lý rõ ràng và có tổ chức.

---

# 📈 Kế hoạch phát triển

## Roadmap

* 🌙 Dark Mode
* ☁️ Google Calendar Sync
* 🧩 Home Screen Widget
* 🤖 AI Assistant
* 💾 Cloud Backup
* 🔄 Multi-device Sync
* 📄 Export PDF
* 👥 Chia sẻ lịch học
* 🏪 Plugin Marketplace

---

# 🤝 Đóng góp

Mọi đóng góp đều được hoan nghênh.

# 👨‍💻 Thành viên phát triển

## WIIS Personal Project

> "Viết app để quản lý lịch học, rồi quên commit code vì bận học."

Dự án được xây dựng nhằm hỗ trợ sinh viên quản lý lịch học, thời khóa biểu và các hoạt động cá nhân một cách hiệu quả bằng các công nghệ Android hiện đại.

---

# 📜 License

Dự án được phát hành theo giấy phép MIT License.

---

# ⭐ Nếu dự án hữu ích

Nếu WIIS giúp bạn:

* Không quên lịch học 📚
* Không bỏ lỡ lịch thi 📝
* Không bị deadline úp sọt 💀

Thì hãy để lại một ⭐ trên GitHub nhé.

---

<p align="center">
  <b>WIIS — Why, It Is Sunday?</b><br>
  Because every student deserves a better calendar. 📅✨
</p>

<p align="center">
  Made with ❤️, Kotlin ☕ và một lượng deadline vừa đủ để tồn tại.
</p>
