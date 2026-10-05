package com.ecosphere.common;

/** String constants shared across the application (kept as strings so Thymeleaf templates keep working). */
public final class Const {
  private Const() {}

  public static final String ROLE_USER = "USER", ROLE_NGO = "NGO", ROLE_NURSERY = "NURSERY", ROLE_ADMIN = "ADMIN";
  public static final String ACTIVE = "ACTIVE", PENDING = "PENDING", SUSPENDED = "SUSPENDED";
  public static final String VERIFIED = "VERIFIED", REJECTED = "REJECTED";
  public static final String CONFIRMED = "CONFIRMED", DELIVERED = "DELIVERED", CANCELLED = "CANCELLED";
  public static final String COD = "COD", BKASH = "BKASH";

  // Plantation requests (ticket 22)
  public static final String REQ_REQUESTED = "REQUESTED", REQ_ACCEPTED = "ACCEPTED", REQ_DECLINED = "DECLINED", REQ_FULFILLED = "FULFILLED";

  // Reports / complaints (ticket 32)
  public static final String REPORT_OPEN = "OPEN", REPORT_IN_REVIEW = "IN_REVIEW", REPORT_RESOLVED = "RESOLVED", REPORT_DISMISSED = "DISMISSED";

  // Badge criteria (ticket 12 / 31)
  public static final String CRIT_VERIFIED_PLANTATIONS = "VERIFIED_PLANTATIONS", CRIT_CONSUMPTION_LOGS = "CONSUMPTION_LOGS",
      CRIT_ECO_POINTS = "ECO_POINTS", CRIT_GREEN_SCORE = "GREEN_SCORE", CRIT_FIRST_SUBMISSION = "FIRST_SUBMISSION";

  // Config keys
  public static final String CFG_POINTS_PER_TREE = "ECOPOINTS_PER_TREE", CFG_LOW_STOCK = "LOW_STOCK_THRESHOLD";
}
