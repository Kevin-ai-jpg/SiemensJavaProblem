package model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "stop_time")
public class StopTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // In StopTime.java - Add @JsonBackReference
    @ManyToOne(optional = false)
    @JsonBackReference
    private Train train;


    @ManyToOne(optional = false)
    private Station station;

    // ordering index in route
    private int stopIndex;

    @Convert(converter = LocalTimeStringConverter.class)
    private LocalTime arrival;

    @Convert(converter = LocalTimeStringConverter.class)
    private LocalTime departure;

    public StopTime() {}
    public StopTime(Train train, Station station, int stopIndex, LocalTime arrival, LocalTime departure) {
        this.train = train;
        this.station = station;
        this.stopIndex = stopIndex;
        this.arrival = arrival;
        this.departure = departure;
    }

    public Long getId() { return id; }
    public Train getTrain() { return train; }
    public Station getStation() { return station; }
    public int getStopIndex() { return stopIndex; }
    public LocalTime getArrival() { return arrival; }
    public LocalTime getDeparture() { return departure; }
}
