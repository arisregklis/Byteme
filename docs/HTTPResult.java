package gr.aueb.byteme.common;

public class HTTPResult {
    private final int status;
    private final String body;

    public HTTPResult(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
    
}

