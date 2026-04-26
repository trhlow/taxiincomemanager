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

```powershell
docker compose up -d
```

Database listen trên `localhost:5433` (host) → container port 5432, db `taxi_income`, user `taxi`, password `taxi_dev_pwd`. (Dùng 5433 thay vì 5432 để tránh đụng với Postgres khác có thể đã cài sẵn trên máy.)

### 2. Chạy backend

```powershell
cd backend
./mvnw spring-boot:run
```

Backend listen trên `http://localhost:8081`. Flyway sẽ tự chạy migration `V1__init.sql` khi khởi động lần đầu. (Dùng 8081 thay vì 8080 mặc định để tránh đụng với service khác.)

Smoke test nhanh bằng curl:

```powershell
# Tạo user lần đầu
curl -X POST http://localhost:8081/api/users/init -H "Content-Type: application/json" -d "{\"displayName\":\"Long\"}"

# Lấy userId từ response, gán vào biến (ví dụ $UID)
$UID = "<paste-userId-here>"

# Nhập đơn
curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -H "X-User-Id: $UID" -d "{\"orderAmount\":444000,\"tipAmount\":0,\"taxiCount\":2}"

# Xem dashboard
curl http://localhost:8081/api/dashboard -H "X-User-Id: $UID"
```

### 3. Chạy mobile app

```powershell
cd mobile
flutter pub get
flutter run
```

Cấu hình base URL backend (có thể chỉnh ở màn hình onboarding):

- Android emulator: `http://10.0.2.2:8081`
- iOS simulator / desktop: `http://localhost:8081`

App sẽ hỏi nhập tên ở lần mở đầu tiên, sau đó lưu `userId` vào `SharedPreferences` và gửi qua header `X-User-Id` ở mọi request.

## API tóm tắt


| Method | Endpoint                                               | Mô tả                                            |
| ------ | ------------------------------------------------------ | ------------------------------------------------ |
| POST   | `/api/users/init`                                      | Tạo user lần đầu (idempotent)                    |
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


Mọi request (trừ `POST /api/users/init`) cần header `X-User-Id`.

## Stop services

```powershell
docker compose down
# hoặc xoá luôn data:
docker compose down -v
```

