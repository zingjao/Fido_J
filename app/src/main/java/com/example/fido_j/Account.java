package com.example.fido_j;

public class Account {
    private String account;
    private String id;
    private response res;

    public Account(String account, String id, response res) {
        this.account = account;
        this.id = id;
        this.res = res;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public static class response{
        private String sessionId;
        private String public_key;

        public String getPublic_key() {
            return public_key;
        }

        public void setPublic_key(String public_key) {
            this.public_key = public_key;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}