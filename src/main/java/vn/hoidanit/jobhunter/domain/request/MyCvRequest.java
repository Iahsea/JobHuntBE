package vn.hoidanit.jobhunter.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MyCvRequest {
    private String name;

    private String url;

    private String size;

    private long userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}