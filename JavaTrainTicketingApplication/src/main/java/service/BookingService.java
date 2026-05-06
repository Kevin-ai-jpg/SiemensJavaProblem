package service;

import model.Booking;
import model.Train;
import repo.BookingRepository;
import repo.TrainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BookingService: uses a JVM-level lock per trainCode to serialize seat allocation.
 * This avoids the SQLite "SELECT ... FOR UPDATE" syntax problem (SQLite doesn't support it).
 *
 * For multi-process concurrency you'd use a DB with row-locks or a distributed lock.
 */
@Service
public class BookingService {
    private final TrainRepository trainRepo;
    private final BookingRepository bookingRepo;
    private final EmailService emailService;

    // per-train lock objects
    private static final ConcurrentHashMap<String, Object> trainLocks = new ConcurrentHashMap<>();

    public BookingService(TrainRepository trainRepo, BookingRepository bookingRepo, EmailService emailService) {
        this.trainRepo = trainRepo;
        this.bookingRepo = bookingRepo;
        this.emailService = emailService;
    }

    /**
     * Attempt to book seats on trainCode for segments from fromIndex to toIndex (stop indexes).
     * Uses a JVM synchronized lock per trainCode to serialize seat allocation so we don't rely
     * on "SELECT ... FOR UPDATE" (which SQLite doesn't support).
     */
    @Transactional
    public Booking book(String trainCode, int fromIndex, int toIndex, int seats, String passengerName, String passengerEmail) {
        // compute/obtain lock for this trainCode
        Object lock = trainLocks.computeIfAbsent(trainCode, k -> new Object());

        synchronized (lock) {
            // reload the train while inside the synchronized block/transaction
            Train train = trainRepo.findByTrainCode(trainCode)
                    .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainCode));

            // compute booked seats per segment
            int segments = Math.max(0, train.getStops().size() - 1);
            int[] bookedPerSegment = new int[Math.max(0, segments)];
            List<Booking> bookings = bookingRepo.findByTrain(train);
            for (Booking b : bookings) {
                int bFrom = b.getFromIndex();
                int bTo = b.getToIndex();
                for (int i = bFrom; i < bTo; i++) {
                    if (i >= 0 && i < bookedPerSegment.length) bookedPerSegment[i] += b.getSeats();
                }
            }

            // check requested segments availability
            for (int i = fromIndex; i < toIndex; i++) {
                int used = (i >= 0 && i < bookedPerSegment.length) ? bookedPerSegment[i] : 0;
                if (used + seats > train.getCapacity()) {
                    throw new IllegalStateException("Not enough seats on segment index " + i);
                }
            }

            // create booking
            Booking booking = new Booking(train, passengerName, passengerEmail, fromIndex, toIndex, seats);
            bookingRepo.save(booking);

            // send confirmation (ConsoleEmailService or SmtpEmailService if enabled)
            String subject = "Booking confirmed - train " + train.getTrainCode();
            String body = "Booking confirmed for " + passengerName + " on train " + train.getTrainCode() +
                    ", seats=" + seats + ", fromIndex=" + fromIndex + " toIndex=" + toIndex;
            emailService.send(passengerEmail, subject, body);

            return booking;
        }
    }

    public List<Booking> listForTrain(Train train) {
        return bookingRepo.findByTrain(train);
    }
}
