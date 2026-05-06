package model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonManagedReference
    private Train train;

    private String passengerName;
    private String passengerEmail;

    // indexes in the train stops: fromIndex .. toIndex (exclusive)
    private int fromIndex;
    private int toIndex;
    private int seats;

    // persisted using LocalDateTimeStringConverter (autoApply = true)
    private LocalDateTime bookedAt = LocalDateTime.now();

    public Booking() {}
    public Booking(Train train, String name, String email, int fromIndex, int toIndex, int seats) {
        this.train = train; this.passengerName = name; this.passengerEmail = email;
        this.fromIndex = fromIndex; this.toIndex = toIndex; this.seats = seats;
    }

    public Long getId() { return id; }
    public Train getTrain() { return train; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerEmail() { return passengerEmail; }
    public int getFromIndex() { return fromIndex; }
    public int getToIndex() { return toIndex; }
    public int getSeats() { return seats; }
    public LocalDateTime getBookedAt() { return bookedAt; }
}
