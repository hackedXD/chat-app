package server;

import java.util.HashMap;
import java.util.Objects;

public class Request {
    private String method;
    private String path;

    private HashMap<String, String> headers;

    public Request(String method, String path) {
        this(method, path, new HashMap<>());
    }

    public Request(String method, String path, HashMap<String, String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }


    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(method, request.method) && Objects.equals(path, request.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}