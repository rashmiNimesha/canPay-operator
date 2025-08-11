package com.example.canpay_operator.utils;

public class Endpoints {
    public static final String SEND_OTP = "/api/v1/auth/send-otp";
    public static final String VERIFY_OTP = "/api/v1/auth/verify-otp";
    public static final String SAVE_USER_PROFILE = "/api/v1/auth/create-profile";
    public static final String GET_OPERATOR_ASSIGNMENT = "/api/v1/operator-assignment/assignment-status/";
    public static final String GET_OPERATOR_FINANCE_DETAILS = "/api/v1/user-service/operator/financial-details";

    public static final String GET_RECENT_TRANSACTIONS = "/api/v1/transactions/bus/%s/operator/%s/recent";

    public static final String UNASSIGNED_BUS = "/api/v1/operator-assignment/operator/remove-bus";
}
