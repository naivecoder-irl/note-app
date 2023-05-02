package com.springbootnote.constants;

public final class SecurityConstants {

    public static final String AUTH_LOGIN_URL = "/api/auth";

    // Signing key for HS512 algorithm
    // You can use the page http://www.allkeysgenerator.com/ to generate all kinds of keys
    public static final String JWT_SECRET = "n2r5u8x/A%D*G-KaPdSgVkYp3s6v9y$B&E(H+MbQeThWmZq4t7w!z%C*F-J@NcRf";

    // JWT token defaults
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Note_";
    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_ISSUER = "note-api";
    public static final String TOKEN_AUDIENCE = "note-app";

    private SecurityConstants() {
        throw new IllegalStateException("Cannot create instance of static util class");
    }
}