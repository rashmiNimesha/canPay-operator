package com.example.canpay_operator.config;

public class ApiConfig {
    public static final String USER_ROLE = "OPERATOR";


    public static String getBaseUrl() {
//    return "http://10.0.2.2:8081"; // Emulator
return "https://api-v1-canpay.sehanw.com";
    }
}