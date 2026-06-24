# Self-Contained QR Code Feature — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `sclera-cloud-device-asset` the sole, in-process owner of QR (generate, store, look up, validate, scan, lifecycle), deleting every dependency on `sclera-cloud-vdms` / `sclera-integrations` / Dapr for QR.

**Architecture:** Port the QR subsystem from the read-only reference `sclera-vdms-edge-server` (entities, repos, services, controller, ZXing generation, iText PDF) **plus** the cloud feature set from `sclera-cloud-vdms` (templates, Excel import/preview, ADC tagging, bulk generation), adapted to Jakarta + PostgreSQL + AWS S3. The four Dapr stub-clients are replaced by local `@Service` beans of the same bean-names; cloud-sync code is **not** ported (it is the coupling being removed).

**Tech Stack:** Spring Boot, JPA/Hibernate (`ddl-auto: update`), PostgreSQL 16 (`sclera_assets`), ZXing, iText (`com.itextpdf:itextpdf`), Apache POI, AWS SDK (`com.amazonaws:aws-java-sdk-s3`), JUnit 5 + Mockito + AssertJ.

**Spec:** `docs/superpowers/specs/2026-06-22-qr-self-contained-device-asset-design.md`

## Global Constraints

- Package root: `io.sclera.*` (NOT `com.sclera`). Spring Boot 2.6.5-era seed; do not modernize.
- Repository package is capital-R: `io.sclera.Repository`.
- Reference repo `sclera-vdms-edge-server` is **read-only** — read to port, never edit.
- Cloud `Vdms` / `Customer_Organisation` entity FKs become plain `String` columns here: `vdms_id`, `customer_org_id`. device-asset has no such entities.
- DB is PostgreSQL. Convert all ported SQL: `ON DUPLICATE KEY UPDATE … VALUES(c)` → `ON CONFLICT (id) DO UPDATE SET c = EXCLUDED.c`; `INSERT … VALUE(…)` → `VALUES(…)`; `IFNULL` → `COALESCE`; remove backticks; `LIMIT a, b` → `LIMIT b OFFSET a`.
- Image storage = AWS S3 via `QrImageStorageService`, with a filesystem impl for `local`/`test`/`dev` profiles so the build needs no AWS creds.
- **Do NOT remove or change** the `qr_code_sync` / cloud-WebSocket / `APICallService` QR-sync / scheduled-sync logic into device-asset — it is intentionally omitted.
- **Never commit on the user's behalf unless they ask.** Steps below include `git` commits per the skill's TDD rhythm; the executor must hold commits until the user authorizes (per the standing never-commit preference). Treat "Commit" steps as "stage + prepare commit message; commit only when the user says so."
- Build/test: `JAVA_HOME` = JBR 21, use `./mvnw`. Prefer Docker-free unit tests (Testcontainers ITs break on Docker 29). Test command base: `./mvnw -q -Dtest=<ClassName> test`.

---

## File Structure

**Create (entities — `src/main/java/io/sclera/models/`):**
- `PropertyQrcode.java`, `PropertyService.java`, `PropertyServiceRequest.java`, `PropertyServiceResponse.java`, `QrCodeTemplate.java`

**Modify (entities):** `QrCode.java` (+`customer_org_id`,`adcQrCodeCheck`), `ClientQrCode.java` (+`adcClientQrCodeCheck`), `Device.java` (ensure `qrcode_count`)

**Create (repos — `src/main/java/io/sclera/Repository/`):**
- `QrCodeRepository.java`, `ClientQrCodeRepository.java`, `GlobalQrcodeRepository.java`, `PropertyQrCodeRepository.java`, `PropertyServiceRepository.java`, `PropertyServiceRequestRepository.java`, `PropertyServiceResponseRepository.java`, `QrCodeTemplateRepository.java`

**Create (query repos — `src/main/java/io/sclera/queryrepository/`):**
- `QrCodeQueryRepository.java` (reuse existing `ClientQrCodeQueryRepository.java`)

**Create (storage — `src/main/java/io/sclera/utils/`):**
- `QrImageStorageService.java` (interface), `S3QrImageStorageService.java`, `FilesystemQrImageStorageService.java`

**Create (services — `src/main/java/io/sclera/service/`):**
- `QrCodeService.java`, `ClientQrCodeService.java`, `GlobalQrcodeService.java`, `PropertyQrcodeService.java`, `QrCodeTemplateService.java`

**Create (controllers — `src/main/java/io/sclera/controller/admin/`):**
- `GlobalQrcodeController.java`, `QrCodeController.java`, `ClientQrCodeController.java`, `QrCodeTemplateController.java`, `AdcTaggingController.java`

**Modify (rewire/cleanup):** `service/DeviceService.java`, `service/LocationService.java`, `service/EssentialService.java`, `scheduler/DeviceAssetJobHandlers.java`, `utils/ResourceUrlConfig.java`, all `application*.yml`, `src/test/resources/schema-pg.sql`

**Delete:** `client/QrCodeClient.java`, `client/ClientQrCodeClient.java`, `client/GlobalQrcodeClient.java`, `client/PropertyQrcodeClient.java` + their 4 tests in `src/test/java/io/sclera/client/`

**Reference (read-only, to port FROM):**
- edge: `sclera-vdms-edge-server/src/main/java/io/sclera/{models,Repository,queryrepository,service,controller/admin,dto}/...`
- cloud extras: `sclera-cloud-vdms/src/main/java/io/sclera/{service,controller/frontend,model,repository,dto}/...`

---

## Phase 0 — Storage abstraction & config

### Task 1: QR image storage abstraction (S3 + filesystem fallback)

**Files:**
- Create: `src/main/java/io/sclera/utils/QrImageStorageService.java`
- Create: `src/main/java/io/sclera/utils/FilesystemQrImageStorageService.java`
- Create: `src/main/java/io/sclera/utils/S3QrImageStorageService.java`
- Test: `src/test/java/io/sclera/utils/FilesystemQrImageStorageServiceTest.java`

**Interfaces:**
- Produces: `interface QrImageStorageService { String store(byte[] bytes, String key, String ext); byte[] fetch(String key); void delete(String key); }` — `key` is the QR id, `ext` is `"png"`/`"jpeg"`/`"pdf"`/`"zip"`; `store` returns the URL persisted in `image_url`.

- [ ] **Step 1: Write the failing test**
```java
package io.sclera.utils;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;

class FilesystemQrImageStorageServiceTest {
    @Test
    void storeWritesFileAndReturnsUrl(@TempDir Path tmp) throws Exception {
        FilesystemQrImageStorageService svc =
            new FilesystemQrImageStorageService(tmp.toString() + "/", "http://host/images/qrcodes/");
        String url = svc.store(new byte[]{1,2,3}, "qr-1", "png");
        assertThat(url).isEqualTo("http://host/images/qrcodes/qr-1.png");
        assertThat(Files.readAllBytes(tmp.resolve("qr-1.png"))).containsExactly(1,2,3);
        assertThat(svc.fetch("qr-1")).containsExactly(1,2,3);
        svc.delete("qr-1");
        assertThat(Files.exists(tmp.resolve("qr-1.png"))).isFalse();
    }
}
```
- [ ] **Step 2: Run test, verify FAIL** — `./mvnw -q -Dtest=FilesystemQrImageStorageServiceTest test` → fails (classes missing).
- [ ] **Step 3: Implement** the interface and filesystem impl:
```java
// QrImageStorageService.java
package io.sclera.utils;
public interface QrImageStorageService {
    String store(byte[] bytes, String key, String ext);
    byte[] fetch(String key);
    void delete(String key);
}
```
```java
// FilesystemQrImageStorageService.java
package io.sclera.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;

@Component @Profile({"local","test","dev","default"})
public class FilesystemQrImageStorageService implements QrImageStorageService {
    private final String absolutePath;
    private final String urlBase;
    public FilesystemQrImageStorageService(
        @Value("${sclera.server-qrcode-images-absolute-path}") String absolutePath,
        @Value("${sclera.server-qrcode-images-url}") String urlBase) {
        this.absolutePath = absolutePath; this.urlBase = urlBase;
    }
    private Path path(String key, String ext) { return Paths.get(absolutePath, key + "." + ext); }
    private Path find(String key) {
        for (String e : new String[]{"png","jpeg","pdf","zip","txt"}) { Path p = path(key,e); if (Files.exists(p)) return p; }
        return path(key,"png");
    }
    public String store(byte[] bytes, String key, String ext) {
        try { Files.createDirectories(Paths.get(absolutePath)); Files.write(path(key,ext), bytes); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        return urlBase + key + "." + ext;
    }
    public byte[] fetch(String key) { try { return Files.readAllBytes(find(key)); } catch (IOException e) { throw new UncheckedIOException(e); } }
    public void delete(String key) { try { Files.deleteIfExists(find(key)); } catch (IOException e) { throw new UncheckedIOException(e); } }
}
```
```java
// S3QrImageStorageService.java  (active in non-local profiles)
package io.sclera.utils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component @Profile({"docker","qa","uat","prod","development"})
public class S3QrImageStorageService implements QrImageStorageService {
    private final AmazonS3 s3; private final String bucket; private final String prefix; private final String urlBase;
    public S3QrImageStorageService(AmazonS3 s3,
        @Value("${sclera.aws.s3.bucket}") String bucket,
        @Value("${sclera.aws.s3.qrcode-prefix:qrcodes/}") String prefix,
        @Value("${sclera.server-qrcode-images-url}") String urlBase) {
        this.s3 = s3; this.bucket = bucket; this.prefix = prefix; this.urlBase = urlBase;
    }
    private String objectKey(String key, String ext) { return prefix + key + "." + ext; }
    public String store(byte[] bytes, String key, String ext) {
        ObjectMetadata m = new ObjectMetadata(); m.setContentLength(bytes.length);
        s3.putObject(bucket, objectKey(key, ext), new ByteArrayInputStream(bytes), m);
        return urlBase + key + "." + ext;
    }
    public byte[] fetch(String key) {
        for (String e : new String[]{"png","jpeg","pdf","zip","txt"}) {
            String ok = objectKey(key, e);
            if (s3.doesObjectExist(bucket, ok)) {
                try { return s3.getObject(bucket, ok).getObjectContent().readAllBytes(); }
                catch (IOException ex) { throw new UncheckedIOException(ex); }
            }
        }
        throw new IllegalArgumentException("QR object not found: " + key);
    }
    public void delete(String key) {
        for (String e : new String[]{"png","jpeg","pdf","zip","txt"}) {
            String ok = objectKey(key, e); if (s3.doesObjectExist(bucket, ok)) s3.deleteObject(bucket, ok);
        }
    }
}
```
- [ ] **Step 4: Run test, verify PASS** — `./mvnw -q -Dtest=FilesystemQrImageStorageServiceTest test` → PASS.
- [ ] **Step 5: Stage commit** (hold per never-commit): `git add src/main/java/io/sclera/utils/QrImageStorageService.java src/main/java/io/sclera/utils/*QrImageStorageService.java src/test/java/io/sclera/utils/FilesystemQrImageStorageServiceTest.java` — message: `feat(qr): add QR image storage abstraction (S3 + filesystem)`.

### Task 2: AWS S3 client bean + config keys

**Files:**
- Create: `src/main/java/io/sclera/config/S3Config.java`
- Modify: `src/main/resources/application.yml`, `application-docker.yml`, `application-local.yml`, `application-test.yml`
- Test: `src/test/java/io/sclera/config/S3ConfigTest.java`

**Interfaces:**
- Produces: a Spring `AmazonS3` bean (only in S3 profiles); config keys `sclera.aws.s3.bucket`, `sclera.aws.s3.region`, `sclera.aws.s3.qrcode-prefix`, `sclera.aws.s3.template-prefix`.

- [ ] **Step 1: Write the failing test** (bean builds from region prop without contacting AWS):
```java
package io.sclera.config;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class S3ConfigTest {
    @Test void buildsClient() {
        AmazonS3 s3 = new S3Config().amazonS3("us-east-1");
        assertThat(s3).isNotNull();
    }
}
```
- [ ] **Step 2: Run, verify FAIL** — `./mvnw -q -Dtest=S3ConfigTest test`.
- [ ] **Step 3: Implement** `S3Config` (uses default credentials provider chain; region from prop):
```java
package io.sclera.config;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
@Configuration @Profile({"docker","qa","uat","prod","development"})
public class S3Config {
    @Bean public AmazonS3 amazonS3(@Value("${sclera.aws.s3.region:us-east-1}") String region) {
        return AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(region)).build();
    }
}
```
- [ ] **Step 4: Add config keys.** Append to each `application*.yml` under `sclera:` (local/test point image url/path at local dirs; docker/prod set bucket via env):
```yaml
  aws:
    s3:
      bucket: ${QR_S3_BUCKET:sclera-qrcodes}
      region: ${QR_S3_REGION:us-east-1}
      qrcode-prefix: qrcodes/
      template-prefix: qrcode-templates/
  server-qrcode-images-url: ${QR_IMAGES_URL:http://localhost:8085/images/qrcodes/}
  server-qrcode-images-absolute-path: ${QR_IMAGES_PATH:/tmp/sclera/images/qrcodes/}
  global-qrcode-server-url: ${GLOBAL_QRCODE_URL:https://app.sclera.com}
  services-cloud-server-url: ${SERVICES_QRCODE_URL:https://app.sclera.com}
```
(Keep keys that already exist; only add the missing ones. `application-test.yml` keeps filesystem paths so no S3 bean loads.)
- [ ] **Step 5: Run, verify PASS** + stage commit: `feat(qr): add S3 client config and QR storage properties`.

### Task 3: Extend `ResourceUrlConfig` with QR getters

**Files:**
- Modify: `src/main/java/io/sclera/utils/ResourceUrlConfig.java`
- Test: `src/test/java/io/sclera/utils/ResourceUrlConfigQrTest.java`

**Interfaces:**
- Produces: `getServer_qrcode_images_url()`, `getServer_qrcode_images_absolute_path()`, `getServices_cloud_server_url()` (and existing `getGlobal_qrcode_server_url()`).

- [ ] **Step 1: Failing test** asserting the new `@Value` fields resolve (use reflection/setters):
```java
package io.sclera.utils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class ResourceUrlConfigQrTest {
    @Test void qrGettersWork() {
        ResourceUrlConfig c = new ResourceUrlConfig();
        c.setServer_qrcode_images_url("u/"); c.setServices_cloud_server_url("s");
        assertThat(c.getServer_qrcode_images_url()).isEqualTo("u/");
        assertThat(c.getServices_cloud_server_url()).isEqualTo("s");
    }
}
```
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — add fields + getters/setters mirroring the existing style:
```java
    @Value("${sclera.server-qrcode-images-url}") private String server_qrcode_images_url;
    @Value("${sclera.server-qrcode-images-absolute-path}") private String server_qrcode_images_absolute_path;
    @Value("${sclera.services-cloud-server-url:https://app.sclera.com}") private String services_cloud_server_url;
    public String getServer_qrcode_images_url(){return server_qrcode_images_url;}
    public void setServer_qrcode_images_url(String v){this.server_qrcode_images_url=v;}
    public String getServer_qrcode_images_absolute_path(){return server_qrcode_images_absolute_path;}
    public void setServer_qrcode_images_absolute_path(String v){this.server_qrcode_images_absolute_path=v;}
    public String getServices_cloud_server_url(){return services_cloud_server_url;}
    public void setServices_cloud_server_url(String v){this.services_cloud_server_url=v;}
```
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): expose QR url config getters`.

---

## Phase 1 — Data layer (entities, repos, query repos)

### Task 4: Extend `QrCode` & `ClientQrCode` entities (ADC + org columns)

**Files:**
- Modify: `src/main/java/io/sclera/models/QrCode.java`, `src/main/java/io/sclera/models/ClientQrCode.java`
- Test: `src/test/java/io/sclera/models/QrCodeEntityTest.java`

**Interfaces:**
- Produces: `QrCode` getters/setters incl. `getCustomerOrgId/setCustomerOrgId` (String), `getAdcQrCodeCheck/setAdcQrCodeCheck` (Integer); `ClientQrCode.getAdcClientQrCodeCheck/set…` (Integer). Existing fields unchanged.

- [ ] **Step 1: Failing test** (POJO round-trip):
```java
package io.sclera.models;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class QrCodeEntityTest {
    @Test void newFieldsRoundTrip() {
        QrCode q = new QrCode();
        q.setCustomerOrgId("org-1"); q.setAdcQrCodeCheck(1);
        assertThat(q.getCustomerOrgId()).isEqualTo("org-1");
        assertThat(q.getAdcQrCodeCheck()).isEqualTo(1);
        ClientQrCode c = new ClientQrCode(); c.setAdcClientQrCodeCheck(1);
        assertThat(c.getAdcClientQrCodeCheck()).isEqualTo(1);
    }
}
```
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — add fields `private String customerOrgId;` + `private Integer adcQrCodeCheck;` to `QrCode` and `private Integer adcClientQrCodeCheck;` to `ClientQrCode`, with matching getters/setters in the existing delombok style. (Hibernate maps to `customer_org_id`, `adc_qr_code_check`, `adc_client_qr_code_check`.) Do not touch existing `@ManyToOne device/location`, audit, or `isDeleted` fields.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add ADC + org columns to QrCode/ClientQrCode`.

### Task 5: New entities — `QrCodeTemplate`

**Files:**
- Create: `src/main/java/io/sclera/models/QrCodeTemplate.java`
- Test: `src/test/java/io/sclera/models/QrCodeTemplateEntityTest.java`

**Interfaces:**
- Produces: entity mapping table `qr_code_template`; fields `id,name,qrTemplateJson,qrCodeTemplateUrl,qrCodeLogoUrl,customerOrgId(String),creationTimestamp(BigInteger),addedBy,updatedTimestamp(BigInteger),updatedBy,inUse(Integer),isDefault(Integer)`; named native queries `QrCodeTemplate.getAllByOrgId`, `.getInUseUrlByOrgId`, `.getDefaultTemplate`, `.getDataByIds` (Postgres SQL).

- [ ] **Step 1: Failing test** (round-trip of fields).
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port `sclera-cloud-vdms/.../model/QrCodeTemplate.java`. Replace `@ManyToOne Customer_Organisation customer_org` (+`@JoinColumn customer_org_id`) with `private String customerOrgId;`. Convert all `@NamedNativeQuery` SQL to Postgres (`LIMIT ? OFFSET ?`, no backticks). Provide getters/setters.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add QrCodeTemplate entity`.

### Task 6: New entities — Property QR graph

**Files:**
- Create: `src/main/java/io/sclera/models/PropertyQrcode.java`, `PropertyService.java`, `PropertyServiceRequest.java`, `PropertyServiceResponse.java`
- Test: `src/test/java/io/sclera/models/PropertyQrcodeEntityTest.java`

**Interfaces:**
- Produces:
  - `PropertyQrcode` (`property_qrcode`): `id`, `image_url`, `@ManyToOne PropertyService property_service`, `@ManyToOne Location location`, `@OneToMany(mappedBy="property_qrcode", cascade=ALL) Set<PropertyServiceResponse> property_service_response`. Named native queries `PropertyQrcode.getPropertyServiceLocations`, `.getPropertyQrcode`, `.getPropertyQrcodeByFloor` → `PropertyQrcodeDTO` (use existing `dto/PropertyQrcodeDTO.java` constructors).
  - `PropertyService` (`property_service`): `id,name,vdmsId(String),...` + `@OneToMany requests`.
  - `PropertyServiceRequest` (`property_service_request`): `id,name,@ManyToOne property_service`.
  - `PropertyServiceResponse` (`property_service_response`): `id,value,@ManyToOne property_qrcode,@ManyToOne property_service_request,updatedAt,updatedBy`.

- [ ] **Step 1: Failing test** (round-trip of PropertyQrcode + a response link).
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port the four entities from `sclera-vdms-edge-server/.../models/` (`PropertyQrcode`, `PropertyService`, `PropertyServiceRequest`, `PropertyServiceResponse`). Replace any `Vdms`/`Customer_Organisation` FK with `String vdmsId`/`String customerOrgId`. Convert `@NamedNativeQuery` SQL to Postgres. Keep `@SqlResultSetMapping propertyqrcodeemapping` mapping to `PropertyQrcodeDTO`.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add property-service QR entity graph`.

### Task 7: `QrCodeQueryRepository` (Postgres upsert)

**Files:**
- Create: `src/main/java/io/sclera/queryrepository/QrCodeQueryRepository.java`
- Test: `src/test/java/io/sclera/queryrepository/QrCodeQueryRepositoryTest.java`

**Interfaces:**
- Produces: `String getQueryForUpsertQrCodesInBatch()` returning a Postgres `ON CONFLICT` upsert with 12 positional params in this order: `id,image_url,location_id,vdms_id,device_id,created_by,creation_time,batch_id,qr_code_link,updated_time,updated_by` (+ literal `false` for `is_deleted`).

- [ ] **Step 1: Failing test:**
```java
package io.sclera.queryrepository;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class QrCodeQueryRepositoryTest {
    @Test void upsertSqlIsPostgres() {
        String sql = new QrCodeQueryRepository().getQueryForUpsertQrCodesInBatch();
        assertThat(sql).contains("INSERT INTO qr_code").contains("ON CONFLICT (id) DO UPDATE SET")
            .contains("image_url = EXCLUDED.image_url").contains("is_deleted = false");
        assertThat(sql).doesNotContain("ON DUPLICATE KEY").doesNotContain("VALUES(");
    }
}
```
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** (mirror existing `ClientQrCodeQueryRepository` style):
```java
package io.sclera.queryrepository;
import org.springframework.stereotype.Component;
@Component
public class QrCodeQueryRepository {
    public String getQueryForUpsertQrCodesInBatch() {
        return "INSERT INTO qr_code (id, image_url, location_id, vdms_id, device_id, created_by, creation_time, batch_id, qr_code_link, updated_time, updated_by, is_deleted) "
             + "VALUES (?,?,?,?,?,?,?,?,?,?,?, false) "
             + "ON CONFLICT (id) DO UPDATE SET "
             + "image_url = EXCLUDED.image_url, location_id = EXCLUDED.location_id, vdms_id = EXCLUDED.vdms_id, "
             + "device_id = EXCLUDED.device_id, created_by = EXCLUDED.created_by, creation_time = EXCLUDED.creation_time, "
             + "batch_id = EXCLUDED.batch_id, qr_code_link = EXCLUDED.qr_code_link, updated_time = EXCLUDED.updated_time, "
             + "updated_by = EXCLUDED.updated_by, is_deleted = false";
    }
}
```
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add Postgres QrCode upsert query repository`.

### Task 8: JPA repositories (8 interfaces)

**Files:**
- Create: `src/main/java/io/sclera/Repository/QrCodeRepository.java`, `ClientQrCodeRepository.java`, `GlobalQrcodeRepository.java`, `PropertyQrCodeRepository.java`, `PropertyServiceRepository.java`, `PropertyServiceRequestRepository.java`, `PropertyServiceResponseRepository.java`, `QrCodeTemplateRepository.java`
- Test: `src/test/java/io/sclera/Repository/QrRepositoriesContextTest.java`

**Interfaces:**
- Produces (method contracts later tasks rely on):
  - `QrCodeRepository`: `Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String>)`, `getQrCodesByLocationIds(Set<String>)`, `getQrCodeDetailsByIds(Set<String>)`, `getClientQrCodeDetailsByIds(Set<String>)` (named native, defined on `QrCode`); `Integer getQrCodeCountByDeviceId(String)`; `JSONArray getDeviceIdsTaggedToQrCode(String)`, `getLocationIdsTaggedToQrCode(String)`; `BigInteger getMaxUpdatedQrCodeTimeStamp(String)`; `long countByDeviceId(String)`; `@Modifying void updateIsDeletedForAllQrCodes()`, `deleteOldQrCodes()`; `int checkQrCodeId(String)`, `int getAdcCheckByQrCodeId(String)`, `int getIsManagedAssetsTagged(String)`.
  - `ClientQrCodeRepository`: analogous on `client_qr_code` (`getClientQrCodeCountByDeviceId`, `getDeviceIdsTaggedToClientQrCode`, `getLocationIdsTaggedToClientQrCode`, `maxUpdatedClientQrCodeTimeStamp`, `countByDeviceId`, `updateIsDeletedForAllClientQrCodes(Set<String>)`, `deleteOldClientQrCodes`, ADC checks).
  - `GlobalQrcodeRepository`: full set from spec §5.1 (`addGlobalQrcode`, `upsertGlobalQrcode`, `getGlobalQrCodeLocation/Device`, `getQrcodeDetail`, `getUntaggedGlobalQrcodes`, `getGlobalQrcodes`, `getGlobalQrcodeById`, `getGlobalQrCodesByIds`, `getImageurlByID`, `deleteGlobalQrcodeById`, `getGlobalQrcodeIdByLocation/Device`, `getDeviceQrcodeCountByDeviceId`, detail variants).
  - `PropertyQrCodeRepository`: `addPropertyQrcode`, `getPropertyServiceLocations`, `getPropertyQrcode`, `getPropertyQrcodeByFloor`, `getPropertyServicesByLocationId`.
  - `PropertyService/Request/ResponseRepository`: CRUD + lookups used by `PropertyQrcodeService`.
  - `QrCodeTemplateRepository`: `addQrCodeTemplate`, `updateQrCodeTemplateById`, `removeQrCodeTemplateByIds`, `updateInUseByOrgId`, `updateInUseByUrl`, `getQrCodeLogoUrlById`, named-native list/in-use/default/byIds.

- [ ] **Step 1: Write a Spring context smoke test** (Docker-free; mocks the DataSource via `@SpringBootTest` slice is heavy — instead assert the interfaces are valid Spring Data repos by loading a minimal `@DataJpaTest` against the test schema IF available; otherwise a compile-time test that the bean types exist):
```java
package io.sclera.Repository;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class QrRepositoriesContextTest {
    @Test void interfacesExtendJpaRepository() {
        assertThat(org.springframework.data.jpa.repository.JpaRepository.class
            .isAssignableFrom(QrCodeRepository.class)).isTrue();
        assertThat(org.springframework.data.jpa.repository.JpaRepository.class
            .isAssignableFrom(GlobalQrcodeRepository.class)).isTrue();
    }
}
```
- [ ] **Step 2: Run, verify FAIL** (interfaces missing).
- [ ] **Step 3: Implement** — port each repository from the edge reference `sclera-vdms-edge-server/src/main/java/io/sclera/Repository/`, applying SQL conversions (MySQL→Postgres) on every `@Query`. For `@Query` that referenced MySQL `INSERT … VALUE` / `ON DUPLICATE KEY`, rewrite to Postgres. Keep `nativeQuery=true` methods bound to the entity `@NamedNativeQuery` names. Use `JpaRepository<Entity,String>`.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add QR JPA repositories (postgres)`.

### Task 9: Add QR tables to test schema

**Files:**
- Modify: `src/test/resources/schema-pg.sql`
- (Verification happens via later service ITs; this task has no standalone test.)

- [ ] **Step 1: Add** `CREATE TABLE IF NOT EXISTS` blocks for `qr_code`, `client_qr_code`, `global_qrcode`, `property_qrcode`, `property_service`, `property_service_request`, `property_service_response`, `qr_code_template`, matching the entity columns + FKs to existing `device`/`location`/`floor`/`building`. Mirror the column types used by the existing tables in this file (varchar ids, bigint timestamps, boolean is_deleted).
- [ ] **Step 2: Verify** the file parses by running any existing `@DataJpaTest`-style test in the module (e.g. `./mvnw -q -Dtest=*RepositoryTest test` if such exist) OR defer verification to Task 12.
- [ ] **Step 3: Stage commit** — `test(qr): add QR tables to pg test schema`.

---

## Phase 2 — Core services + dependency removal

### Task 10: `GlobalQrcodeService` (generate / lookup / scan / export / delete)

**Files:**
- Create: `src/main/java/io/sclera/service/GlobalQrcodeService.java`
- Test: `src/test/java/io/sclera/service/GlobalQrcodeServiceTest.java`

**Interfaces:**
- Consumes: `GlobalQrcodeRepository`, `QrImageStorageService`, `ResourceUrlConfig`, `DeviceService` (for `qrcode_count`).
- Produces (bean name `globalQrcodeService`): `void createGlobalQrcode(String username,String vdmsid,Integer count)`, `Set<GlobalQrcodeDTO> getGlobalQrCode(String,String,String,String,Integer,Integer,JSONObject)`, `void upsertGlobalQrcode(String,String,Set<GlobalQrcodeDTO>)`, `void deleteGlobalQrcode(String,String,Set<String>)`, `void exportGlobalQrCodes(HttpServletResponse,...)`, `List<GlobalQrcodeDTO> getQrcodeDetail(String,String,GlobalQrcodeDTO)`, `Integer getDeviceQrcodeCountByDeviceId(String)`, `void deleteGlobalQRCodeByLocationId(String)`.

- [ ] **Step 1: Failing test** — generation persists a record + stores an image (mock repo + storage), and payload is well-formed:
```java
package io.sclera.service;
import io.sclera.Repository.GlobalQrcodeRepository;
import io.sclera.utils.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalQrcodeServiceTest {
    @Test void createGeneratesImageAndPersists() {
        GlobalQrcodeRepository repo = mock(GlobalQrcodeRepository.class);
        QrImageStorageService storage = mock(QrImageStorageService.class);
        when(storage.store(any(), anyString(), anyString())).thenReturn("http://img/x.png");
        ResourceUrlConfig cfg = new ResourceUrlConfig(); cfg.setGlobal_qrcode_server_url("https://app");
        GlobalQrcodeService svc = new GlobalQrcodeService(repo, storage, cfg, mock(DeviceService.class));
        svc.createGlobalQrcode("u","vdms-1",1);
        verify(storage, times(1)).store(any(), anyString(), eq("png"));
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(repo, times(1)).addGlobalQrcode(anyString(), url.capture(), any(), any());
        assertThat(url.getValue()).isEqualTo("http://img/x.png");
    }
}
```
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port `sclera-vdms-edge-server/.../service/GlobalQrcodeService.java` with these adaptations: constructor injection of the 4 collaborators above; replace `addFileToServer(...)` filesystem writes with `storage.store(bytes, id, "png")`; replace image deletes with `storage.delete(id)`; build the QR data string from `ResourceUrlConfig.getGlobal_qrcode_server_url()` per spec §5.5; keep ZXing (`QRCodeWriter`, 500×500, `ErrorCorrectionLevel.H`); keep iText export but read image bytes via `storage.fetch(id)`. Use `jakarta.servlet.http.HttpServletResponse`. **Do not** port any cloud/APICall/sync code.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add local GlobalQrcodeService`.

### Task 11: `QrCodeService` + `ClientQrCodeService` (local reads/counts/lookups; no sync)

**Files:**
- Create: `src/main/java/io/sclera/service/QrCodeService.java`, `src/main/java/io/sclera/service/ClientQrCodeService.java`
- Test: `src/test/java/io/sclera/service/QrCodeServiceTest.java`, `ClientQrCodeServiceTest.java`

**Interfaces:**
- Consumes: `QrCodeRepository`, `ClientQrCodeRepository`, `QrCodeQueryRepository`/`ClientQrCodeQueryRepository`, `DataSource`, `QrImageStorageService`, `ResourceUrlConfig`.
- Produces (bean names `qrCodeService`, `clientQrCodeService`) — every method the consumers call, delegating to repos:
  - `QrCodeService`: `Integer getQrCodeCountByDeviceId(String)`, `Set<QrCodeDTO> getQrCodesByDeviceIds(Set<String>)`, `getQrCodesByLocationIds(Set<String>)`, `JSONArray getDeviceIdsTaggedToQrCode(String)`, `getLocationIdsTaggedToQrCode(String)`, `BigInteger getMaxUpdatedQrCodeTimeStamp(String)`, `Integer countByDeviceId(String)`, `Set<QrCodeDTO> getQrCodeDetailsByIds(Set<String>)`, `getClientQrCodeDetailsByIds(Set<String>)`. Plus cloud-parity (Phase 4): generation/tag/untagged/counts.
  - `ClientQrCodeService`: `Integer getClientQrCodeCountByDeviceId(String)`, `JSONArray getDeviceIdsTaggedToClientQrCode(String)`, `getLocationIdsTaggedToClientQrCode(String)`, `BigInteger maxUpdatedClientQrCodeTimeStamp(String)`, `Integer countByDeviceId(String)`.

- [ ] **Step 1: Failing test** per service — e.g. `getQrCodeCountByDeviceId` delegates to repo and returns its value (mock repo); `countByDeviceId` returns `(int) repo.countByDeviceId`.
```java
// QrCodeServiceTest.java (representative)
package io.sclera.service;
import io.sclera.Repository.QrCodeRepository;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
class QrCodeServiceTest {
    @Test void countDelegatesToRepo() {
        QrCodeRepository repo = mock(QrCodeRepository.class);
        when(repo.getQrCodeCountByDeviceId("d1")).thenReturn(3);
        QrCodeService svc = new QrCodeService(); svc.qrCodeRepository = repo;
        assertThat(svc.getQrCodeCountByDeviceId("d1")).isEqualTo(3);
    }
}
```
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port the edge `QrCodeService`/`ClientQrCodeService` **read/count/lookup** methods only (the JDBC batch `upsert*InBatch` helper may be ported for reuse by generation, but **omit** all `sync*`, `APICallService`, paging-from-cloud logic). Use field injection matching the test (`@Autowired` fields) or constructor injection — match the existing service style in this module (most services use `@Autowired` fields). Keep `getClientQrCodeDetailsByIds` delegating to `QrCodeService`/`QrCodeRepository` named query.
- [ ] **Step 4: Run, verify PASS** for both test classes.
- [ ] **Step 5: Stage commit** — `feat(qr): add local QrCodeService and ClientQrCodeService`.

### Task 12: Repository integration smoke (optional, env-gated)

**Files:**
- Test: `src/test/java/io/sclera/Repository/GlobalQrcodeRepositoryIT.java`

- [ ] **Step 1:** Write a `@DataJpaTest`(`@AutoConfigureTestDatabase(replace=NONE)` with the `schema-pg.sql` + an embedded/locally-available Postgres) that inserts a `global_qrcode` and asserts `getDeviceQrcodeCountByDeviceId`/`getQrcodeDetail` return expected rows. **Gate with `@EnabledIfSystemProperty(named="qr.it",matches="true")`** so it is skipped in the default Docker-free run.
- [ ] **Step 2: Run** the normal suite to confirm it is skipped: `./mvnw -q test`.
- [ ] **Step 3: Stage commit** — `test(qr): add gated GlobalQrcode repository IT`.

### Task 13: Replace Dapr clients — rewire `DeviceService`

**Files:**
- Modify: `src/main/java/io/sclera/service/DeviceService.java`
- Delete: `src/main/java/io/sclera/client/QrCodeClient.java`, `ClientQrCodeClient.java`, `GlobalQrcodeClient.java`
- Delete: `src/test/java/io/sclera/client/QrCodeClientTest.java`, `ClientQrCodeClientTest.java`, `GlobalQrcodeClientTest.java`
- Test: compile + existing DeviceService tests.

**Interfaces:**
- Consumes: `QrCodeService`, `ClientQrCodeService`, `GlobalQrcodeService` (Tasks 10–11).

- [ ] **Step 1:** Change the autowired field **types** (keep names) in `DeviceService`:
  - `io.sclera.client.GlobalQrcodeClient globalQrcodeService` → `io.sclera.service.GlobalQrcodeService globalQrcodeService`
  - `io.sclera.client.QrCodeClient qrCodeService` → `io.sclera.service.QrCodeService qrCodeService`
  - `io.sclera.client.ClientQrCodeClient clientQrCodeService` → `io.sclera.service.ClientQrCodeService clientQrCodeService`
  - `private io.sclera.client.QrCodeClient qrCodeRepository` → `io.sclera.service.QrCodeService qrCodeRepository`
  - `private io.sclera.client.ClientQrCodeClient clientQrCodeRepository` → `io.sclera.service.ClientQrCodeService clientQrCodeRepository`
- [ ] **Step 2:** Verify each call-site (lines ~4933, 5432-33, 5852, 5953, 6000, 6356-57, 6444-45, 6477-78, 9133-34, 9208, 10817-22, 10926-27) compiles against the new method signatures. The new services expose identical method names/return types, so no body changes should be needed; fix any `JSONArray`/`Set` cast mismatches the same way the old code did.
- [ ] **Step 3:** Delete the 3 client classes + 3 client tests listed above.
- [ ] **Step 4: Run** `./mvnw -q -Dtest=DeviceServiceTest test` (and `./mvnw -q -DskipTests compile`) → compiles, tests pass.
- [ ] **Step 5: Stage commit** — `refactor(qr): rewire DeviceService to local QR services; drop Dapr clients`.

### Task 14: Rewire `LocationService` + remaining property client

**Files:**
- Modify: `src/main/java/io/sclera/service/LocationService.java`
- Delete: `src/main/java/io/sclera/client/PropertyQrcodeClient.java`, `src/test/java/io/sclera/client/PropertyQrcodeClientTest.java`
- Test: compile + existing LocationService tests.

**Interfaces:**
- Consumes: `QrCodeService`, `ClientQrCodeService`, `GlobalQrcodeService`, `PropertyQrcodeService` (Task 15).

- [ ] **Step 1:** Swap autowired types (keep names) in `LocationService`: `propertyQrcodeService`→`io.sclera.service.PropertyQrcodeService`, `globalQrcodeService`→`io.sclera.service.GlobalQrcodeService`, `qrCodeService`→`io.sclera.service.QrCodeService`, `clientQrCodeService`→`io.sclera.service.ClientQrCodeService`. Update the `import io.sclera.client.PropertyQrcodeClient;` to the service package.
- [ ] **Step 2:** Verify the ~20 call-sites (387, 538-540, 668, 720-21, 823, 897-98, 1077-78, 1136-37, 1218-19, 1253-54, 1352-53, 1504-05, 1592-93) compile against new signatures (same names). `deleteGlobalQRCodeByLocationId` and `updatePropertyServiceLocations` now resolve to the local services.
- [ ] **Step 3:** Delete the property client + its test. (This step depends on Task 15 existing — order Task 15 before 14 if executing strictly; see sequencing note.)
- [ ] **Step 4: Run** `./mvnw -q -Dtest=LocationServiceTest test` + `compile`.
- [ ] **Step 5: Stage commit** — `refactor(qr): rewire LocationService; drop PropertyQrcodeClient`.

### Task 15: `PropertyQrcodeService` (local; no cloud sync/WebSocket)

**Files:**
- Create: `src/main/java/io/sclera/service/PropertyQrcodeService.java`
- Test: `src/test/java/io/sclera/service/PropertyQrcodeServiceTest.java`

**Interfaces:**
- Consumes: `PropertyQrCodeRepository`, `PropertyServiceRepository`, `PropertyServiceRequestRepository`, `PropertyServiceResponseRepository`, `QrImageStorageService`, `ResourceUrlConfig`, `BuildingService` (existing).
- Produces (bean name `propertyQrcodeService`): `updatePropertyServiceLocations(String)`, `PropertyServiceDTO upsertPropertyServiceDetails(...)`, `addPropertyServiceLocations(...)`, `String generateQrcode(String)`, `multiUpdatePropertyServiceResponse(...)`, `Set<PropertyQrcodeDTO> getPropertyServiceLocationsById(...)`, delete methods, `Set<PropertyQrcodeDTO> getZoneMap(...)`. (Methods that were cloud-only become local-only or no-ops.)

- [ ] **Step 1: Failing test** — `generateQrcode` stores a JPEG and returns its URL (mock storage); `updatePropertyServiceLocations` cascades via repo (mock).
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port edge `PropertyQrcodeService.java`, removing `APICallService` cloud calls and `socketService.socketPropertyServiceValueUpdate(...)` (drop the cloud WebSocket entirely; if a local notification is wanted, use device-asset's existing websocket infra — otherwise omit). Replace filesystem image writes with `storage.store(bytes, id, "jpeg")` and deletes with `storage.delete(id)`. QR payload base = `ResourceUrlConfig.getServices_cloud_server_url()`.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add local PropertyQrcodeService`.

### Task 16: Neutralize the scheduled sync + EssentialService

**Files:**
- Modify: `src/main/java/io/sclera/scheduler/DeviceAssetJobHandlers.java`, `src/main/java/io/sclera/service/EssentialService.java`
- Test: `src/test/java/io/sclera/scheduler/DeviceAssetJobHandlersTest.java` (if present) or compile.

- [ ] **Step 1:** Confirm `qrcodeNfcBarcodeSync()` remains a no-op (already a stub). Leave it logging "no-op (QR is local, no sync)" or remove the job registration if nothing else references it. Ensure the commented QR sync block in `EssentialService` stays removed/commented.
- [ ] **Step 2:** Add a one-line code comment documenting that QR sync is intentionally absent (device-asset owns QR).
- [ ] **Step 3: Run** `./mvnw -q -DskipTests compile`.
- [ ] **Step 4: Stage commit** — `chore(qr): document removal of QR sync job`.

---

## Phase 3 — Controllers + scan API

### Task 17: `GlobalQrcodeController` (generate / list / scan / export / delete)

**Files:**
- Create: `src/main/java/io/sclera/controller/admin/GlobalQrcodeController.java`
- Test: `src/test/java/io/sclera/controller/admin/GlobalQrcodeControllerTest.java`

**Interfaces:**
- Consumes: `GlobalQrcodeService`.
- Produces: REST endpoints (paths from edge `GlobalQrcodeContoller`): `POST /user/{username}/vdms/{vdmsid}/type/{qrcode_type}/getqrcodes`, `DELETE …/deleteqrcodes`, `GET …/exportqrcodes` (PDF), `POST …/upsertglobalqrcode`, `GET …/count/{count}/createqrcode`, `POST …/getqrcodedetail` (scan).

- [ ] **Step 1: Failing test** — MockMvc (standalone) verifying `getqrcodedetail` delegates to `service.getQrcodeDetail(...)` and returns 200 + JSON. Use `MockMvcBuilders.standaloneSetup(controller)` with a mocked service.
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port the controller, swapping `javax.*`→`jakarta.*`, wiring `GlobalQrcodeService`. Keep method signatures/paths identical.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add GlobalQrcodeController (generate/scan/export)`.

### Task 18: cloud-parity controllers (QR / ClientQR / ADC)

**Files:**
- Create: `src/main/java/io/sclera/controller/admin/QrCodeController.java`, `ClientQrCodeController.java`, `AdcTaggingController.java`
- Test: one MockMvc test per controller (`QrCodeControllerTest`, `ClientQrCodeControllerTest`, `AdcTaggingControllerTest`)

**Interfaces:**
- Consumes: `QrCodeService`, `ClientQrCodeService`.
- Produces: endpoints mirroring `sclera-cloud-vdms` `QRCodeController` / `ClientQrCodeController` / `AdcTaggingTouchscreenController` paths (generate, generateBulk, updateQrCode, getQrCodeDetailsBy*, getUnTaggedQrCode, getQrCodeCounts, getQrCodeCheckById; client tag/import/preview/lookup/untagged/check; tag…ByVdmsId).

- [ ] **Step 1: Failing test** per controller (one endpoint each via standalone MockMvc, mocked service).
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port the three cloud controllers; `javax`→`jakarta`; inject local services; remove any `HttpServletRequest`-based multi-tenant/cloud params not needed locally (keep signatures otherwise). For bulk-generation endpoints, call the async generation added in Task 19.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add QR/ClientQR/ADC controllers`.

---

## Phase 4 — Cloud-parity features (generation, templates, Excel, ADC, bulk)

### Task 19: QR generation + bulk export in `QrCodeService`

**Files:**
- Modify: `src/main/java/io/sclera/service/QrCodeService.java`
- Test: `src/test/java/io/sclera/service/QrCodeServiceGenerationTest.java`

**Interfaces:**
- Produces: `byte[] generateQRCode(...)` (single/batch, customizable width/height, type zip/pdf/txt) returning the export bytes / storing to S3; `void generateBulkQRCode(String orgId,String email,Integer count,...)` (async via `ExecutorService`; stores ZIP/PDF/TXT to S3, returns/sends a link); `String upsertQrcode(QrCodeDTO,...)`, `getQrCodeDetailsByQrCodeId`, `getQrCodeDetailsByVdmsIdAndDeviceId/LocationId`, `getUnTaggedQrCode`, `getQrCodeCounts`, `tagQrCodeByVdmsId`, `getAdcCheckByQrCodeId`, `updateQrcodeById`.

- [ ] **Step 1: Failing test** — `generateQRCode(count=1,type="txt")` returns non-empty bytes and persists one `qr_code` row via the JDBC upsert (mock `DataSource`/repo); ZXing produces a decodable image (decode back with ZXing `QRCodeReader` and assert the embedded URL).
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port `sclera-cloud-vdms/.../service/QrCodeService.java` generation/export/tag methods. Replace AWS-presigned-cloud specifics with `QrImageStorageService`; replace `Vdms`/`Customer_Organisation` with String ids; PDF via iText (templates resolved by `QrCodeTemplateService`, Task 20); email delivery via the existing mail infra or a profile-gated `EmailService` stub that logs the link if mail is unavailable (must not throw). Omit multi-tenant websocket/`WebClientService` sync.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add QR generation + bulk export (S3)`.

### Task 20: `QrCodeTemplateService` + controller

**Files:**
- Create: `src/main/java/io/sclera/service/QrCodeTemplateService.java`, `src/main/java/io/sclera/controller/admin/QrCodeTemplateController.java`
- Test: `src/test/java/io/sclera/service/QrCodeTemplateServiceTest.java`

**Interfaces:**
- Consumes: `QrCodeTemplateRepository`, `QrImageStorageService` (template/logo images to S3 `template-prefix`).
- Produces (bean `qrCodeTemplateService`): `addQrCodeTemplate`, `updateQrCodeTemplate`, `deleteQrCodeTemplate`, `getAllQrCodeTemplateByOrgId`, `getInUseUrlByOrgId`, `updateInUseByUrl`, `getDefaultTemplate`. Controller mirrors `QrCodeTemplateController` paths.

- [ ] **Step 1: Failing test** — `addQrCodeTemplate` stores template+logo via storage and persists row (mock repo+storage).
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port from cloud; `MultipartFile`→bytes→`storage.store(..., "png")` under template prefix; String org id; Postgres queries.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add QR code templates (service + controller)`.

### Task 21: Client-QR Excel import/preview + ClientQrCode controller wiring

**Files:**
- Modify: `src/main/java/io/sclera/service/ClientQrCodeService.java`
- Test: `src/test/java/io/sclera/service/ClientQrCodeImportTest.java`

**Interfaces:**
- Produces: `String importClientQrCode(String orgId,String email,MultipartFile file,...)`, `List<ClientQrCodeDTO> previewExcelSheet(String vdmsId,MultipartFile file,...)`, `tagClientQrCode(...)`, `getClientQrCodeDetailsBy*`, `getUnTaggedClientQrCode`, ADC checks.

- [ ] **Step 1: Failing test** — build an in-memory XLSX (POI) with headers `client_qr_code_id,device_id,location_id,vdms_id` + one valid row; assert `previewExcelSheet` marks it `is_validated=1`; an invalid (both device+location) row gets a validation message.
- [ ] **Step 2: Run, verify FAIL.**
- [ ] **Step 3: Implement** — port cloud `ClientQrCodeService` import/preview/tag (POI parsing, header + device-XOR-location validation, same-vdms check); persist via `ClientQrCodeQueryRepository` upsert; String org id; no cloud sync.
- [ ] **Step 4: Run, verify PASS.**
- [ ] **Step 5: Stage commit** — `feat(qr): add client-QR Excel import/preview + tagging`.

---

## Phase 5 — Migration & final validation

### Task 22: Idempotent Postgres schema script

**Files:**
- Create: `infra/postgres-init/30-qr-schema.sql`
- Test: (DDL applied by Postgres init; verified in Task 23 checklist.)

- [ ] **Step 1: Write** the script: `CREATE TABLE IF NOT EXISTS` for `property_qrcode`, `property_service`, `property_service_request`, `property_service_response`, `qr_code_template`; `ALTER TABLE … ADD COLUMN IF NOT EXISTS` for `qr_code.customer_org_id`, `qr_code.adc_qr_code_check`, `client_qr_code.adc_client_qr_code_check`, `device.qrcode_count`; `CREATE UNIQUE INDEX IF NOT EXISTS` on `client_qr_code(client_qr_code_id)`, `global_qrcode(device_id)`, `global_qrcode(location_id)`; `CREATE INDEX IF NOT EXISTS` on `qr_code(device_id)`,`qr_code(location_id)`,`qr_code(vdms_id)`,`client_qr_code(device_id)`,`client_qr_code(location_id)`,`property_qrcode(location_id)`,`property_qrcode(property_service_id)`,`qr_code_template(customer_org_id)`. Use the `IF NOT EXISTS` form everywhere (matches `01-schemas.sql`/`20-workorder-db.sql` style). Add a header comment noting it complements Hibernate `ddl-auto`.
- [ ] **Step 2: Validate SQL** by applying it to a scratch Postgres (`docker run --rm -e POSTGRES_PASSWORD=x -v $PWD/infra/postgres-init:/init postgres:16` then `psql -f`), or by `psql --dry-run`-style parse if Docker unavailable; confirm no errors and re-running is idempotent.
- [ ] **Step 3: Stage commit** — `feat(qr): add idempotent Postgres QR schema script`.

### Task 23: Final verification & cleanup sweep

**Files:** none (verification); fix-ups as needed.

- [ ] **Step 1: No-VDMS grep** — confirm zero matches:
  `grep -rE "QrCodeClient|ClientQrCodeClient|GlobalQrcodeClient|PropertyQrcodeClient" src/main` → none;
  `grep -rE "invokeMethod\(.*sclera-integrations.*[Qq]r" src/main` → none;
  `grep -riE "syncQrCode|syncClientQrCode|syncAlQrCodes|multiTenantSyncApiCall|socketPropertyServiceValueUpdate" src/main` → none (or only comments).
- [ ] **Step 2: Build** — `./mvnw -q -DskipTests compile` → success.
- [ ] **Step 3: Unit tests** — `./mvnw -q test` → green (Docker-free).
- [ ] **Step 4: Run the spec §9 checklist** — generation, lookup, scan, validation, lifecycle, consumer counts, FK integrity, data intact. Note any gaps and open follow-ups in `migration-notes/`.
- [ ] **Step 5: Update `migration-notes/`** with the QR ownership migration summary + decisions (String-FK reconciliation, sync removal, S3 storage). Stage commit — `docs(qr): record QR ownership migration in migration-notes`.

---

## Sequencing note
Strict dependency order: **1→2→3 → 4→5→6→7→8→9 → 10→11→(12) → 15 → 13→14 → 16 → 17→18 → 19→20→21 → 22→23.**
(Task 15 `PropertyQrcodeService` must exist before Task 14 deletes `PropertyQrcodeClient`; Tasks 10–11 before Task 13. Within subagent-driven execution, dispatch in this order.)

## Self-review summary
- **Spec coverage:** ownership/architecture → T10,11,13,14,17; entities/schema → T4,5,6,22; repos/query → T7,8; generation → T10,19; storage S3 → T1,2; templates → T20; Excel/ADC → T18,21; scan/lookup/lifecycle → T10,17; dependency removal → T13,14,16; migration → T22; validation → T23. No uncovered sections.
- **Placeholders:** none — concrete code for novel pieces; for large faithful ports, exact source file + transformation rules + produced signatures are given (a port task cannot inline thousands of reference lines; the rules + signature contract are the actionable unit).
- **Type consistency:** service bean names (`qrCodeService`, `clientQrCodeService`, `globalQrcodeService`, `propertyQrcodeService`, `qrCodeRepository`/`clientQrCodeRepository` aliases) match the consumer fields in T13/T14; repository method names in T8 match service usage in T10/T11/T19.
