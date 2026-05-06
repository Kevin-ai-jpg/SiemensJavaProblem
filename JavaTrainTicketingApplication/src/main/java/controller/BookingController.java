package controller;

import model.Booking;
import service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    public static class BookingRequest {
        public String trainCode;
        public int fromIndex;
        public int toIndex;
        public int seats;
        public String passengerName;
        public String passengerEmail;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingRequest r) {
        try {
            Booking b = bookingService.book(r.trainCode, r.fromIndex, r.toIndex, r.seats, r.passengerName, r.passengerEmail);
            return ResponseEntity.ok(b);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
