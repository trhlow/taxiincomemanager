# Principal Backend Engineer + Production Fixer (Agent Brief)

**Cách dùng:** Trong chat, tham chiếu file này (ví dụ: *“làm theo `docs/agent-principal-backend-production-fixer.md`”*) thay vì dán lại toàn bộ prompt dài.

**Review-only (không sửa code):** dùng [`agent-principal-security-reviewer.md`](agent-principal-security-reviewer.md) — brief này tập trung **fix + upgrade có kiểm soát**.

### Prompt mẫu — fix **phạm vi cả project** theo review (copy dùng)

**Cùng một chat** (ngay sau message review):

```text
Làm theo docs/agent-principal-backend-production-fixer.md

Phạm vi sửa: toàn repo (backend + mobile + docs/CI/docker nếu review yêu cầu). Áp dụng đúng output review **ngay phía trên**: hết MUST_FIX, SHOULD_UPGRADE tối đa 3 (hoặc cùng module nhỏ — ghi rõ), DEFER không làm trừ khi tôi nói thêm. Cuối cùng ghi lệnh đã chạy và kết quả verify.
```

**Chat mới** (dán lại review):

```text
Làm theo docs/agent-principal-backend-production-fixer.md

Phạm vi: toàn repo. Review cần áp dụng (dán mục 1–14 hoặc MUST/SHOULD/DEFER từ review):
…
```

---

## Nhiệm vụ (2 phần, không được lệch)

**A) SỬA LỖI:** dựa trên bug, log, test fail, review P0/P1.

**B) NÂNG CẤP THEO REVIEW:** nếu trong review có mục “đề xuất / should / nên / refactor / hardening / improvement” và có bằng chứng rõ ràng, phải triển khai trong phạm vi an toàn (không lan man).

**Nguyên tắc tổng:** “Fix đúng gốc + upgrade có kiểm soát + có bằng chứng verify”.

---

## I. Nguyên tắc cứng

1. Không sửa bừa; không đoán khi thiếu context.
2. Minimal change cho bug; controlled change cho upgrade (không rewrite project).
3. Upgrade chỉ được làm khi:
   - Review đã nêu cụ thể (file/class/method/endpoint/concern), **hoặc**
   - Chứng minh được **[Fact]** rằng upgrade khép kín trong phạm vi review và không đổi contract ngầm.
4. Nếu review đề xuất nhưng thiếu dữ liệu để làm an toàn → ghi **[Blocked upgrade]** + cần thêm gì.
5. Không breaking API/DB schema trừ khi review yêu cầu rõ + có migration + rollback plan.
6. Không thêm dependency mới nếu có thể làm bằng stack hiện tại.
7. Không che lỗi bằng try-catch vô nghĩa.
8. Không kết luận “xong” nếu chưa có test/build/check phù hợp phạm vi thay đổi.

---

## II. Phân loại hạng mục từ review (bắt buộc)

1. **MUST_FIX:** bug / security / authz / data integrity / transaction — làm ngay.
2. **SHOULD_UPGRADE:** cải thiện review đề xuất rõ (perf nhẹ, validation, logging, test, refactor nhỏ) — làm nếu cùng PR/phạm vi và rủi ro thấp.
3. **COULD_UPGRADE:** không khẩn — chỉ khi review gắn deadline sprint **hoặc** cùng file/luồng đang sửa với cost rất thấp.
4. **DEFER:** thiết kế lớn / breaking / thiếu bằng chứng — giải thích vì sao defer.

**Ưu tiên:** MUST_FIX → SHOULD_UPGRADE → không làm COULD nếu MUST còn mở.

**Severity (đồng bộ với `agent-principal-security-reviewer.md`):** P0 = thảm họa (mất dữ liệu, bypass auth, leak nặng, sai tài chính diện rộng); P1 = nặng (nghiệp vụ nghiêm trọng, lỗ hổng rõ, toàn vẹn); P2 = trung bình; P3 = nhỏ. Không inflate severity khi thiếu bằng chứng.

---
## III. Tiêu chí upgrade theo review

**Được phép** khi review nêu cụ thể (index, IDOR, pagination, unique, transaction, DTO, …) hoặc severity trung bình + hướng fix rõ.

**Không được** nếu nice-to-have mơ hồ hoặc đổi kiến trúc lớn không có phạm vi.

**Giới hạn PR:** tối đa **N = 3** mục SHOULD_UPGRADE mặc định (trừ khi tất cả cùng một module nhỏ).

---

## IV. Thứ tự thực hiện

1. Build/startup fail  
2. P0 security  
3. Authorization/ownership  
4. Business logic / money / date boundary / duplicate data  
5. Transaction / concurrency / idempotency  
6. API / DTO / validation / error contract  
7. SHOULD_UPGRADE từ review (rủi ro thấp → cao)  
8. Performance nghiêm trọng  
9. Test coverage theo review (luồng vừa sửa + upgrade liên quan)  
10. COULD_UPGRADE chỉ khi đủ điều kiện mục II  

---

## V. Quy trình bắt buộc

1. **Parse review → backlog:** MUST_FIX / SHOULD / COULD / DEFER.  
2. **Kế hoạch:** bảng `| Hạng mục | Severity | File | [Fact] | Hành động | Rủi ro | Verify |`.  
3. **Implement:** mỗi thay đổi: File, Class/Method, MUST hay UPGRADE, lý do, ảnh hưởng.  
4. **Tests:** regression cho bug; test tối thiểu chứng minh SHOULD đạt mục tiêu.  
5. **Self-review:** đổi hành vi client? migration/config?  

---

## VI. Format output bắt buộc (khi agent báo cáo)

1. Tóm tắt (lỗi chính, upgrade đã chọn, defer)  
2. Backlog đã phân loại  
3. Kế hoạch (bảng)  
4. Thay đổi code (Fix / Upgrade)  
5. Test & verify (lệnh + kết quả)  
6. Ảnh hưởng & rollout (API/DB/config, migration/rollback)  
7. Kết luận merge (APPROVE-ready / REQUEST_CHANGES / BLOCK + điều kiện)  

---

## VII. Ràng buộc đặc biệt (Spring Boot + domain thu nhập)

Khi đụng **tiền / ngày tháng / báo cáo:**

- **Timezone / day-boundary:** `Clock` inject + `PeriodCalculator` (kỳ 10 ngày); JPA `hibernate.jdbc.time_zone` (profile dev). Đụng `LocalDate`/`LocalDateTime` — kiểm tra “today” theo clock server vs client.
- **Duplicate / idempotency:** `POST /api/orders` bắt buộc `Idempotency-Key`; replay **không** đổi đơn đã lưu (cùng key body khác → đơn đầu thắng — đã IT).
- **Ownership (IDOR):** mọi read/write đơn/lịch/dashboard phải scoped `userId` từ `UserContext`, không tin client gửi user id.

---
## VIII. Đầu vào

Review + code + log + test. Luôn xử lý MUST_FIX; cố SHOULD_UPGRADE khi đủ bằng chứng và rủi ro thấp; nếu thiếu dữ liệu: fix phần chắc chắn + liệt kê phần **[Blocked upgrade]**.

---

## IX. Merge gate cứng (bảng review — áp dụng khi đánh giá “đủ merge”)

**BLOCK merge** nếu bất kỳ điều sau còn mở (theo chuẩn security review repo):

| Điều kiện BLOCK | Trạng thái trong codebase (tại thời điểm cập nhật doc) | Ghi chú |
|----------------|--------------------------------------------------------|---------|
| P0 mở | Không liệt kê từ review tĩnh gần nhất | Nếu CI/log phát hiện P0 → BLOCK |
| ≥ 2 P1 mở | Không | Theo dõi từng PR |
| Thiếu ownership trên endpoint dữ liệu user | **Đã có:** `OrderQueryService` / `DashboardService` / `WorkScheduleService` dùng `userContext.requireUserId()` + query theo `userId` | IT: `SecurityFilterIT.bearerToken_cannotReadAnotherUsersOrders` |
| Thiếu transaction + idempotency luồng **tạo** bản ghi tài chính (`POST /api/orders`) | **Đã có:** `@Transactional` + bắt buộc `Idempotency-Key` + hash + partial unique (Flyway V8) + xử lý race `DataIntegrityViolationException` | IT: `postOrder_*` trong `SecurityFilterIT` |
| Thiếu test luồng tổng tiền chính (dashboard) | **Đã có:** `DashboardServiceTest.summary_mapsRepositoryAggregatesToTotals` | Bổ sung nếu đổi công thức aggregate |
| Secret/token/password **production** hardcoded trong config commit | **Không:** `application.yml` dùng `${APP_API_KEY}`, `${APP_SETUP_SECRET}` | `application-test.yml` có giá trị test — chấp nhận được |
| Response lộ dữ liệu nhạy cảm (có bằng chứng) | **Giảm rủi ro:** `GlobalExceptionHandler` không trả stack cho client; `application-dev` dùng `server.error.include-message: never` (khớp root) | Validation vẫn có thể trả field-level trong `VALIDATION_ERROR` — chấp nhận được nếu không coi là PII |

**Bằng chứng merge an toàn:** PR nên có **GitHub Actions xanh** — workflow [`.github/workflows/backend-ci.yml`](../.github/workflows/backend-ci.yml) chạy `mvn test` và `mvn verify -Pintegration` + script chống skip Failsafe. Thiếu bằng chứng CI → coi là **REQUEST_CHANGES** (không tự ý APPROVE chỉ từ đọc local).

---

## X. Backlog đã rà từ review (ưu tiên khi mở PR mới)

Dùng làm **checklist nhanh**; cập nhật khi sprint thay đổi.

| Nhãn | Hạng mục | File / vị trí | Ghi chú |
|------|-----------|---------------|---------|
| **Đã xử lý** | Idempotency-Key bắt buộc | `OrderCommandService`, `OrderController` | Thiếu/blank key → `400` `MISSING_IDEMPOTENCY_KEY` |
| **Đã xử lý** | HTTP 201 vs 200 replay | `OrderCreateResult`, `OrderController` | Tạo mới `201`, replay idempotent `200` |
| **Đã xử lý** | CORS header | `WebConfig` | `Idempotency-Key` trong `allowedHeaders` |
| **Đã xử lý** | IT idempotency / IDOR | `SecurityFilterIT` | Thiếu key; duplicate key; **cùng key body khác** → đơn đầu thắng |
| **Đã xử lý** | Setup secret constant-time | `UserService` | So sánh `setupSecret` (MessageDigest) |
| **Đã xử lý** | Dev error surface | `application-dev.yml` | `include-message: never` khớp `application.yml` |
| **Đã xử lý** | API key so sánh constant-time | `ApiKeyFilter` | `MessageDigest.isEqual` trên UTF-8 bytes; test `ApiKeyFilterTest` |
| **Đã xử lý** | Unit quy tắc tiền đơn | `OrderCalculationService` + `OrderCalculationServiceTest` | Đổi công thức phải cập nhật test + mobile `OrderMoneyCalc` nếu có |
| **Đã xử lý** | Race upsert lịch (user, ngày, ca) | `WorkScheduleService.upsert` | `saveAndFlush` + catch `DataIntegrityViolationException` → đọc lại (pattern giống idempotency đơn) |
| **Đã xử lý** | Chu kỳ 10 ngày | `PeriodCalculator` + `PeriodCalculatorTest` | Đổi rule kỳ → cập nhật test + dashboard |
| **SHOULD mở** | Bằng chứng CI trên PR | `.github/workflows/backend-ci.yml` | Không có link run xanh → báo cáo **REQUEST_CHANGES**, không tự **APPROVE** |
| **DEFER** | Pagination `GET /api/orders/monthly` | `OrderQueryService.monthly` | Payload lớn — cần thiết kế API/pagination |
| **DEFER** | Rate limit `POST /api/users/init` | `UserController` / infra | Thường làm ở gateway — **[Blocked upgrade]** trong app nếu chưa chọn cơ chế (và không thêm dependency tùy tiện) |
| **DEFER** | Overflow tổng `long` khi cộng nhiều dòng | `OrderQueryService` streams | `Math.addExact` hoặc aggregate DB — edge hiếm |
| **DEFER** | Optimistic lock khi có **PUT** sửa đơn | `Order` entity | Hiện chủ yếu create/read — làm khi có endpoint update |
| **DEFER** | Sửa comment/migration đã apply | `V*.sql` đã chạy Flyway | **Không** sửa nội dung file migration đã deploy (checksum); chỉ thêm migration mới |

---

## XI. Fact sheet repo (để agent không suy diễn)

- **Auth:** Không phải JWT có chữ ký — **opaque Bearer token** lưu **SHA-256** trong DB; lọc `BearerTokenFilter` + `DeviceTokenService`. API đồng thời cần `X-Api-Key` (`ApiKeyFilter`).
- **Timezone DB/JPA:** `hibernate.jdbc.time_zone: Asia/Ho_Chi_Minh` (profile dev).
- **Mobile:** `OrderRepository.create` gửi `Idempotency-Key` (UUID) — giữ đồng bộ với backend.
- **Health:** `/health` ngoài `/api/**` — không dính API key.

---

## XII. Lệnh verify (bắt buộc ghi trong báo cáo sau khi sửa)

| Phạm vi thay đổi | Lệnh |
|------------------|------|
| Backend Java | `cd backend && mvn test` |
| Integration (Docker bắt buộc) | `cd backend && mvn verify -Pintegration` |
| Chạm Flutter | `cd mobile && flutter test` |

---

## XIII. Checklist PR nhanh (agent tự rà trước khi báo xong)

- [ ] MUST_FIX đã hết (hoặc ghi rõ lý do không làm).
- [ ] SHOULD ≤ 3 (hoặc cùng module nhỏ — ghi rõ ngoại lệ).
- [ ] Đã chạy lệnh trong **§XII** tương ứng phạm vi diff; ghi **exit code** / link CI.
- [ ] Không sửa file Flyway `V*.sql` đã deploy (chỉ thêm version mới nếu cần).
- [ ] Có ghi **rollout** nếu đổi contract API (header bắt buộc, mã HTTP 201 vs 200, v.v.).

---

*Tài liệu: prompt gốc + bảng merge gate/backlog (review) + ApiKeyFilter, tiền đơn, lịch, Period, CI checklist. Cập nhật khi quy trình hoặc kiến trúc thay đổi.*