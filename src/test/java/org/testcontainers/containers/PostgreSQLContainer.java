package org.testcontainers.containers;

public class PostgreSQLContainer<T> {
    public PostgreSQLContainer(String image) { }
    public String getJdbcUrl() { return "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; }
    public String getUsername() { return "sa"; }
    public String getPassword() { return ""; }
}
