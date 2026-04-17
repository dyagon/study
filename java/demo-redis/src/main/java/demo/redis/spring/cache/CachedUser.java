package demo.redis.spring.cache;

import java.io.Serializable;
import java.util.Objects;

/**
 * 演示缓存用的用户实体。
 */
public class CachedUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String email;

    public CachedUser() {}

    public CachedUser(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedUser that = (CachedUser) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CachedUser{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
