package service;

import java.time.LocalTime;
import java.util.List;

public class JourneyDto {
    public static class Leg {
        public String trainCode;
        public String from;
        public String to;
        public LocalTime depart;
        public LocalTime arrive;
        public int availableSeats;

        public Leg(String trainCode, String from, String to, LocalTime depart, LocalTime arrive, int availableSeats) {
            this.trainCode = trainCode; this.from = from; this.to = to; this.depart = depart; this.arrive = arrive;
            this.availableSeats = availableSeats;
        }
    }

    public List<Leg> legs;
    public JourneyDto(List<Leg> legs) { this.legs = legs; }
}
