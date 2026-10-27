package com.ecosphere.backend;

import com.ecosphere.repository.TreeRepo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Backend (REST API) tests: security rules, registration and the buy-tree order flow. Runs on H2, no MySQL needed. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiIntegrationTest {
  static final String PW = "password123";
  @Autowired TestRestTemplate rest;
  @Autowired TreeRepo trees;

  private HttpEntity<Object> as(String email, Object body) {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    if (email != null) h.setBasicAuth(email, PW);
    return new HttpEntity<>(body, h);
  }

  private Long firstTreeId() { return trees.findAll().get(0).getId(); }

  // ---------- security ----------
  @Test void protectedEndpointWithoutLoginIsUnauthorized() {
    assertEquals(HttpStatus.UNAUTHORIZED, rest.exchange("/api/user/orders", HttpMethod.GET, as(null, null), String.class).getStatusCode());
  }
  @Test void wrongPasswordIsUnauthorized() {
    HttpHeaders h = new HttpHeaders(); h.setBasicAuth("afia@ecosphere.com", "wrong-password");
    assertEquals(HttpStatus.UNAUTHORIZED, rest.exchange("/api/user/orders", HttpMethod.GET, new HttpEntity<>(h), String.class).getStatusCode());
  }
  @Test void userCannotReachAdminApi() {
    assertEquals(HttpStatus.FORBIDDEN, rest.exchange("/api/admin/users", HttpMethod.GET, as("afia@ecosphere.com", null), String.class).getStatusCode());
  }
  @Test void nurseryCannotPlaceUserOrder() {
    ResponseEntity<String> r = rest.exchange("/api/user/orders", HttpMethod.POST, as("nursery@ecosphere.com",
        Map.of("treeId", firstTreeId(), "quantity", 1, "address", "x", "phone", "01712345678", "payment", "COD")), String.class);
    assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
  }
  @Test void speciesCatalogueIsPublic() {
    assertEquals(HttpStatus.OK, rest.getForEntity("/api/species", String.class).getStatusCode());
  }

  // ---------- registration ----------
  @Test void registerNewUserSucceeds() {
    Map<String, Object> body = Map.of("name", "Test User", "email", "api.user." + System.nanoTime() + "@test.com", "phone", "01711111111",
        "location", "Dhaka", "password", PW, "confirm", PW, "role", "USER", "termsAccepted", true);
    ResponseEntity<Map> r = rest.exchange("/api/auth/register", HttpMethod.POST, as(null, body), Map.class);
    assertEquals(HttpStatus.CREATED, r.getStatusCode());
    assertEquals("USER", r.getBody().get("role"));
  }
  @Test void registerRejectsPasswordMismatch() {
    Map<String, Object> body = Map.of("name", "T", "email", "mismatch@test.com", "phone", "01711111111",
        "password", PW, "confirm", "different123", "role", "USER", "termsAccepted", true);
    assertEquals(HttpStatus.BAD_REQUEST, rest.exchange("/api/auth/register", HttpMethod.POST, as(null, body), String.class).getStatusCode());
  }
  @Test void registerRejectsDuplicateEmail() {
    Map<String, Object> body = Map.of("name", "T", "email", "afia@ecosphere.com", "phone", "01711111111",
        "password", PW, "confirm", PW, "role", "USER", "termsAccepted", true);
    assertEquals(HttpStatus.BAD_REQUEST, rest.exchange("/api/auth/register", HttpMethod.POST, as(null, body), String.class).getStatusCode());
  }
  @Test void registerRequiresTerms() {
    Map<String, Object> body = Map.of("name", "T", "email", "noterms@test.com", "phone", "01711111111",
        "password", PW, "confirm", PW, "role", "USER", "termsAccepted", false);
    assertEquals(HttpStatus.BAD_REQUEST, rest.exchange("/api/auth/register", HttpMethod.POST, as(null, body), String.class).getStatusCode());
  }

  // ---------- buy tree / orders ----------
  @Test void userPlacesCodOrderAndSeesItInList() {
    Map<String, Object> body = Map.of("treeId", firstTreeId(), "quantity", 2, "address", "House 1, Mirpur", "phone", "01712345678", "payment", "COD");
    ResponseEntity<Map> r = rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com", body), Map.class);
    assertEquals(HttpStatus.CREATED, r.getStatusCode());
    ResponseEntity<String> list = rest.exchange("/api/user/orders", HttpMethod.GET, as("afia@ecosphere.com", null), String.class);
    assertEquals(HttpStatus.OK, list.getStatusCode());
    assertTrue(list.getBody().contains("House 1, Mirpur"));
  }
  @Test void orderReducesTreeStock() {
    var t = trees.findAll().get(1); int before = t.getQuantity();
    rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com",
        Map.of("treeId", t.getId(), "quantity", 3, "address", "Addr", "phone", "01712345678", "payment", "COD")), Map.class);
    assertEquals(before - 3, trees.findById(t.getId()).orElseThrow().getQuantity());
  }
  @Test void orderCannotExceedStock() {
    ResponseEntity<String> r = rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com",
        Map.of("treeId", firstTreeId(), "quantity", 9999, "address", "Addr", "phone", "01712345678", "payment", "COD")), String.class);
    assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
  }
  @Test void orderRequiresAddress() {
    ResponseEntity<String> r = rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com",
        Map.of("treeId", firstTreeId(), "quantity", 1, "address", " ", "phone", "01712345678", "payment", "COD")), String.class);
    assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
  }
  @Test void bkashOrderNeedsValidSenderAndTrxId() {
    ResponseEntity<String> bad = rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com",
        Map.of("treeId", firstTreeId(), "quantity", 1, "address", "A", "phone", "01712345678", "payment", "BKASH", "bkashSender", "123", "trxId", "AB")), String.class);
    assertEquals(HttpStatus.BAD_REQUEST, bad.getStatusCode());
    ResponseEntity<String> ok = rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com",
        Map.of("treeId", firstTreeId(), "quantity", 1, "address", "A", "phone", "01712345678", "payment", "BKASH", "bkashSender", "01712345678", "trxId", "API" + (System.nanoTime() % 1000000000L))), String.class);
    assertEquals(HttpStatus.CREATED, ok.getStatusCode());
  }
  @Test void duplicateBkashTrxIdIsRejected() {
    String trx = "DUP" + (System.nanoTime() % 1000000000L);
    Map<String, Object> body = Map.of("treeId", firstTreeId(), "quantity", 1, "address", "A", "phone", "01712345678", "payment", "BKASH", "bkashSender", "01712345678", "trxId", trx);
    assertEquals(HttpStatus.CREATED, rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com", body), String.class).getStatusCode());
    assertEquals(HttpStatus.BAD_REQUEST, rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com", body), String.class).getStatusCode());
  }
  @Test void userCanCancelOwnPendingOrder() {
    ResponseEntity<Map> r = rest.exchange("/api/user/orders", HttpMethod.POST, as("afia@ecosphere.com",
        Map.of("treeId", firstTreeId(), "quantity", 1, "address", "A", "phone", "01712345678", "payment", "COD")), Map.class);
    Object id = r.getBody().get("id");
    ResponseEntity<String> c = rest.exchange("/api/user/orders/" + id + "/cancel", HttpMethod.POST, as("afia@ecosphere.com", null), String.class);
    assertEquals(HttpStatus.OK, c.getStatusCode());
  }
}
