# Taxi Income Manager

App cá nhân quản lý thu nhập tài xế taxi: nhập đơn, tính cước/thực nhận, tổng hợp theo chu kỳ 10 ngày, lịch làm việc tuần.

## Kiến trúc

```
Flutter mobile app  --HTTP-->  Spring Boot REST API  --JPA-->  PostgreSQL
```

- `backend/` — Spring Boot 3.3 + Java 21 + Flyway + PostgreSQL
- `mobile/` — Flutter (Material 3, Riverpod, GoRouter)
- `docker-compose.yml` — PostgreSQL 16 cho dev

## Công thức tính tiền

Mỗi đơn:

```
feeAmount = orderAmount * feeRate                    (mặc định feeRate = 0.30)
subtotal  = orderAmount - feeAmount + tipAmount      (= "Tổng sau cước + bo")
netAmount = subtotal / 2  nếu taxiCount = 2          (đi chung tài xế khác)
          = subtotal      nếu taxiCount = 1          ("Tiền tôi nhận")
```

Chu kỳ 10 ngày trong tháng:

- Kỳ 1: ngày 01 → 10
- Kỳ 2: ngày 11 → 20
- Kỳ 3: ngày 21 → cuối tháng

## Yêu cầu công cụ

- Docker Desktop (cho PostgreSQL)
- Java 21 (Temurin / Oracle / OpenJDK)
- Maven 3.9+ (hoặc dùng `./mvnw` đi kèm)
- Flutter SDK (stable channel)

## Chạy local

### 1. Khởi động database

Tạo file `.env` từ mẫu:

```powershell
Copy-Item .env.example .env
```

```powershell
docker compose up -d
```

Database listen trên `localhost:5433` (host) → container port 5432. Tên DB, user, password, API key và setup secret lấy từ `.env`. (Dùng 5433 thay vì 5432 để tránh đụng với Postgres khác có thể đã cài sẵn trên máy.)

`docker-compose.yml` **healthcheck** dùng `$$POSTGRES_USER` / `$$POSTGRES_DB` trong container để luôn khớp các biến `POSTGRES_*` (tránh báo unhealthy dù Postgres chạy vì lệnh `pg_isready` sai user/db).

### Kiểm thử backend trong CI và local

- **GitHub Actions:** workflow [`.github/workflows/backend-ci.yml`](.github/workflows/backend-ci.yml) chạy `mvn test` (unit) và `mvn verify -Pintegration` (integration Testcontainers trên Ubuntu — có Docker sẵn). CI sẽ fail nếu integration tests bị skip.
- **Local — chỉ unit test:**

```powershell
cd backend
mvn test
```

- **Local / CI — integration (cần Docker cho Testcontainers PostgreSQL):**

```powershell
cd backend
mvn verify -Pintegration
```

### 2. Chạy backend

```powershell
cd backend
./mvnw spring-boot:run
```

Backend listen trên `http://localhost:8081`. Flyway sẽ tự chạy migration `V1__init.sql` khi khởi động lần đầu. (Dùng 8081 thay vì 8080 mặc định để tránh đụng với service khác.)

Smoke test nhanh bằng curl:

```powershell
$API_KEY = "dev-local-api-key"
$SETUP_SECRET = "dev-local-setup-secret"

# Tạo user lần đầu (response có `accessToken` — chỉ hiển thị một lần)
$response = curl -s -X POST http://localhost:8081/api/users/init -H "Content-Type: application/json" -H "X-Api-Key: $API_KEY" -d "{\"displayName\":\"Long\",\"setupSecret\":\"$SETUP_SECRET\"}"
# Gán token (PowerShell ví dụ: $TOKEN = ($response | ConvertFrom-Json).accessToken )
$TOKEN = "<paste-accessToken-here>"

# Nhập đơn
curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -H "X-Api-Key: $API_KEY" -H "Authorization: Bearer $TOKEN" -d "{\"orderAmount\":444000,\"tipAmount\":0,\"taxiCount\":2}"

# Xem dashboard
curl http://localhost:8081/api/dashboard -H "X-Api-Key: $API_KEY" -H "Authorization: Bearer $TOKEN"
```

### 3. Chạy mobile app

```powershell
cd mobile
flutter pub get
flutter run
```

Nếu đổi `APP_API_KEY` trong `.env`, chạy Flutter với cùng key:

```powershell
flutter run --dart-define=TAXI_API_KEY="<APP_API_KEY>"
```

Cấu hình base URL backend (có thể chỉnh ở màn hình onboarding):

- Android emulator: `http://10.0.2.2:8081`
- iOS simulator / desktop: `http://localhost:8081`

App sẽ hỏi nhập tên và setup secret ở lần mở đầu tiên, sau đó gọi `POST /api/users/init` và lưu **`accessToken`** (opaque) cùng `userId`. Token được lưu bằng secure storage, hết hạn sau 90 ngày, và được thu hồi khi reset/logout khỏi thiết bị. Mọi request sau init gửi `Authorization: Bearer <accessToken>` và header `X-Api-Key`.

Debug Android builds allow cleartext only for `localhost`, `127.0.0.1`, and emulator host `10.0.2.2`. Release builds do not allow cleartext traffic; use HTTPS for a real device/release deployment.

### Android release signing

Release builds are not signed with the debug key. Provide either `mobile/android/key.properties` (not committed) or environment variables:

```properties
storeFile=/absolute/path/to/release-keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

```powershell
$env:ANDROID_KEYSTORE_PATH="C:\path\release-keystore.jks"
$env:ANDROID_KEYSTORE_PASSWORD="..."
$env:ANDROID_KEY_ALIAS="..."
$env:ANDROID_KEY_PASSWORD="..."
```

Keystore files and `key.properties` are ignored by git. Release build tasks fail when no release signing config is available.

## API tóm tắt


| Method | Endpoint                                               | Mô tả                                            |
| ------ | ------------------------------------------------------ | ------------------------------------------------ |
| POST   | `/api/auth/logout`                                     | Thu hồi access token hiện tại                    |
| POST   | `/api/users/init`                                      | Tạo user lần đầu bằng setup secret               |
| GET    | `/api/users/me`                                        | Lấy thông tin user hiện tại                      |
| POST   | `/api/orders`                                          | Nhập đơn mới (auto-tính cước/thực nhận)          |
| GET    | `/api/orders/by-date?date=YYYY-MM-DD`                  | Đơn theo ngày                                    |
| GET    | `/api/orders/monthly?year=2026&month=4`                | Đơn theo tháng                                   |
| GET    | `/api/orders/period/current`                           | Đơn của chu kỳ 10 ngày hiện tại                  |
| GET    | `/api/dashboard`                                       | Tổng hợp ngày/chu kỳ/tháng/bo/cước/ngày làm việc |
| POST   | `/api/schedules`                                       | Đăng ký 1 ca (sáng/tối) cho 1 ngày               |
| DELETE | `/api/schedules?workDate=YYYY-MM-DD&shiftType=MORNING` | Bỏ đăng ký ca                                    |
| GET    | `/api/schedules/week?weekStart=YYYY-MM-DD`             | Lịch tuần                                        |
| GET    | `/api/schedules/week/check?weekStart=YYYY-MM-DD`       | Kiểm tra đủ 1 sáng + 2 tối                       |


Mọi request cần header `X-Api-Key`. Riêng `POST /api/users/init` còn cần `setupSecret` và chỉ dùng được khi DB chưa có user; nếu đã khởi tạo sẽ trả `409 USER_ALREADY_INITIALIZED`. Mọi request sau khi init user cần thêm `Authorization: Bearer <accessToken>` (lấy từ response init; không dùng `X-User-Id` làm credential).

## Stop services

```powershell
docker compose down
# hoặc xoá luôn data:
docker compose down -v
```
