package model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "train")
public class Train {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // In Train.java - Add @JsonManagedReference
    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopIndex")
    @JsonManagedReference
    private List<StopTime> stops = new ArrayList<>();


    @Column(nullable = false, unique = true)
    private String trainCode; // e.g., T1

    private String name;
    private int capacity;

    public Train() {}
    public Train(String trainCode, String name, int capacity) {
        this.trainCode = trainCode; this.name = name; this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getTrainCode() { return trainCode; }
    public void setTrainCode(String trainCode) { this.trainCode = trainCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<StopTime> getStops() { return stops; }
}
