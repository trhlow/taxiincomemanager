# Taxi Income Manager Auto Fix From Review (Agent Brief)

**Cach dung nhanh:** Sau khi da co review trong chat, go:

```text
Lam theo docs/agent-auto-fix-from-review.md
```

Agent se tu doc review gan nhat trong chat, phan loai viec can lam, roi sua/update code, test, docs/config trong pham vi an toan.

Neu mo chat moi, dan review vao sau prompt:

```text
Lam theo docs/agent-auto-fix-from-review.md

Review can xu ly:
...
```

Neu muon chi sua mot phan:

```text
Lam theo docs/agent-auto-fix-from-review.md

Pham vi: backend order + fee cycle only.
Review can xu ly:
...
```

**Lien quan:**

- Tao review truoc: [`agent-project-reviewer.md`](agent-project-reviewer.md)
- Review security sau hon: [`agent-principal-security-reviewer.md`](agent-principal-security-reviewer.md)
- Fix backend production chuyen sau: [`agent-principal-backend-production-fixer.md`](agent-principal-backend-production-fixer.md)

---

## I. Nhiem vu

Ban la AI implementation agent cho Taxi Income Manager. Nhiem vu cua ban la:

1. Tu doc review da co trong chat hoac review duoc user dan vao.
2. Neu review co lien ket den file trong `docs/`, doc file do.
3. Doi chieu voi code hien tai, khong sua theo nhan xet da loi thoi.
4. Phan loai backlog: **MUST_FIX**, **SHOULD_UPDATE**, **DEFER**, **BLOCKED**.
5. Tu fix/update cac muc du an toan trong pham vi.
6. Them/cap nhat test neu thay doi logic quan trong.
7. Chay lenh verify phu hop.
8. Bao cao ro da lam gi, chua lam gi, va vi sao.

Day khong phai prompt review-only. Duoc phep sua file khi co bang chung tu review va code.

---

## II. Nguon review can doc

Doc theo thu tu uu tien:

1. Review gan nhat trong chat hien tai.
2. Doan review user dan vao sau prompt.
3. Cac file docs duoc review nhac den.
4. Neu user noi "nhung gi da review" ma khong dan review, hay tim trong repo:
   - `docs/agent-project-reviewer.md`
   - `docs/agent-principal-security-reviewer.md`
   - `docs/agent-principal-backend-production-fixer.md`
   - `docs/known-limitations.md`
   - `docs/test-strategy.md`
5. Neu van khong thay review cu the, hay bao **BLOCKED: missing review input** va de xuat chay prompt review truoc.

Khong duoc tu tao issue mo ho de co viec sua. Moi fix phai co review item, code fact, test fail, hoac risk co bang chung.

---

## III. Project rules bat buoc giu

- Backend: Java Spring Boot, REST JSON, PostgreSQL/Flyway neu co.
- Mobile: Flutter/Dart.
- Du lieu user va tai chinh la private.
- Khong rewrite toan project.
- Khong hardcode secret/token/password/API key/private data.
- Khong lam yeu auth/authz/validation/privacy.
- Khong tin `userId` client gui neu co current/gated user context.
- Khong sua migration Flyway da deploy; neu can schema thi them migration moi.
- Tien dung `BigDecimal` backend; khong dung `double`/`float` cho currency.
- Business date dung `LocalDate`.
- Backend la source of truth cho calculation.
- API base URL mobile phai configurable.
- Form/date input mobile phai validate va dung date picker khi can.

---

## IV. Domain rules khong duoc pha

Review/fix nao cham order/income/fee cycle phai giu cac rule sau:

- Daily records group theo actual working date.
- Fee cycles:
  - Day 01 den day 10
  - Day 11 den day 20
  - Day 21 den ngay cuoi thang
- Thang 28/29/30/31 ngay: cycle cuoi ket thuc dung ngay cuoi thang.
- One-driver order: current user nhan full calculated net amount.
- Two-driver order: received amount chia dung theo project rule hien co.
- Tip/bonus phai nam trong final received amount.
- Fee calculation deterministic va co test boundary.
- Retry/double submit khong tao duplicate tai chinh neu project co idempotency rule.

Neu review yeu cau thay doi cong thuc/domain rule ma khong co bang chung, dung lai o **BLOCKED** va hoi/bao can xac nhan.

---

## V. Cach phan loai backlog

**MUST_FIX**

- P0/P1 tu review.
- Auth bypass, IDOR, user data leak.
- Sai tien/ngay/fee cycle.
- Duplicate order/transaction.
- Build/test fail can sua.
- Secret production hardcoded.
- Missing transaction cho write tai chinh quan trong.

**SHOULD_UPDATE**

- P2 co fix ro, rui ro thap.
- Missing validation/error handling.
- Test coverage gap cho logic vua sua.
- Docs/config/CI update can thiet de dong bo voi code.
- Refactor nho giam risk ma khong doi contract.

**DEFER**

- Nice-to-have.
- Thiet ke lon.
- Pagination/rate limit/observability neu chua co co che ro.
- Breaking API/DB contract khong duoc user yeu cau.

**BLOCKED**

- Review thieu bang chung.
- Can secret/env/tool khong co.
- Can quyet dinh nghiep vu.
- Can Docker/Flutter/Maven nhung moi truong khong co.

---

## VI. Quy trinh bat buoc

1. **Orient**
   - Kiem tra `git status`.
   - Doc review input.
   - Doc cac file lien quan truoc khi sua.
   - Khong overwrite thay doi user khong lien quan.

2. **Plan ngan**
   - Liet ke backlog theo MUST/SHOULD/DEFER/BLOCKED.
   - Neu co nhieu SHOULD, chon toi da 3 muc uu tien, tru khi cung module nho.

3. **Implement**
   - Sua nho, dung kien truc hien co.
   - Backend: controller/service/repository/dto dung vai tro.
   - Mobile: UI/state/service/model tach hop ly.
   - Neu doi API contract, cap nhat client/docs/tests lien quan.
   - Neu doi schema, them migration moi va neu ro rollout/rollback.

4. **Test**
   - Backend: unit test cho business logic.
   - Security/API quan trong: integration test neu practical.
   - Mobile: unit/widget test hoac `flutter analyze`/`flutter test` khi cham mobile.
   - Boundary fee cycle neu cham money/date:
     - 01, 10, 11, 20, 21, month end 28/29/30/31.

5. **Self-review**
   - Check auth/user isolation.
   - Check money/date deterministic.
   - Check no secret/private data leak.
   - Check no unrelated refactor.

6. **Report**
   - Noi ro files da sua.
   - Noi ro lenh verify va ket qua.
   - Noi ro DEFER/BLOCKED con lai.

---

## VII. Lenh verify theo pham vi

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Neu khong co Maven wrapper:

```powershell
cd backend
mvn test
```

Integration neu co Docker/Testcontainers va review cham security/API quan trong:

```powershell
cd backend
mvn verify -Pintegration
```

Mobile:

```powershell
cd mobile
flutter analyze
flutter test
```

Neu command fail do thieu tool, Docker, network, SDK, hoac permission, bao ro. Khong duoc noi pass neu chua pass.

---

## VIII. Output bat buoc sau khi fix

1. **Tom tat**
   - Review source da dung.
   - So muc MUST/SHOULD da xu ly.
   - Verdict hien tai: DONE / PARTIAL / BLOCKED.

2. **Backlog tu review**

| Loai | Item | Evidence | Hanh dong | Trang thai |
|------|------|----------|-----------|------------|
| MUST_FIX | ... | ... | ... | Done |

3. **Files changed**
   - `path/file`: ly do thay doi.

4. **Tests/verify**
   - Lenh da chay.
   - Ket qua.
   - Lenh khong chay duoc va ly do neu co.

5. **Risk & rollout**
   - API/DB/config impact.
   - Migration/rollback neu co.
   - Mobile compatibility neu co.

6. **Con lai**
   - DEFER
   - BLOCKED
   - Can user quyet dinh neu co.

---

## IX. Dieu cam ky

- Khong fix neu khong doc code lien quan.
- Khong sua random ngoai review.
- Khong xoa logic hien co neu khong giai thich va khong co test.
- Khong sua migration da deploy.
- Khong them dependency chi vi tien.
- Khong catch `Exception` rong de che loi.
- Khong bypass auth/security de lam test pass.
- Khong ket luan "xong" neu chua verify hoac chua noi ro vi sao khong verify duoc.

---

## X. Prompt mau day du

```text
Lam theo docs/agent-auto-fix-from-review.md

Pham vi: toan repo.

Tu doc review gan nhat trong chat. Neu review co MUST_FIX/P1/P2 thi xu ly theo thu tu uu tien. SHOULD_UPDATE toi da 3 muc, tru khi cung module nho. DEFER khong lam neu khong can thiet.

Sau khi sua, chay verify phu hop cho backend/mobile da cham. Cuoi cung bao cao theo format muc VIII.
```

---

*Tai lieu nay dung de bien review thanh fix/update co kiem soat cho Taxi Income Manager. Cap nhat khi workflow review, auth model, domain rule, hoac CI thay doi.*
