package com.example.fido_j.credentials;

public class Credentials {
    private String id;
    private String publicKeys;

    public Credentials(String id, String publicKeys) {
        this.id = id;
        this.publicKeys = publicKeys;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicKeys() {
        return publicKeys;
    }

    public void setPublicKeys(String publicKeys) {
        this.publicKeys = publicKeys;
    }
}
