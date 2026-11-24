package gr.aueb.byteme.common;

public class HttpResult {
    private final int status;
    private final String body;

    public HttpResult(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public Strinig getBody() {
        return body;
    }
}
