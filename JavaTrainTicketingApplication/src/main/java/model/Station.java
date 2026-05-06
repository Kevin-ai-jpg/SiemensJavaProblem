package model;

import jakarta.persistence.*;

@Entity
@Table(name = "station")
public class Station {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;

    // Constructors, getters, setters
    public Station() {}
    public Station(String code, String name) { this.code = code; this.name = name; }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
