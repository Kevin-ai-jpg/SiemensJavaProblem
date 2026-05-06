package service;

import model.Booking;
import model.Station;
import model.StopTime;
import model.Train;
import repo.BookingRepository;
import repo.StationRepository;
import repo.TrainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final TrainRepository trainRepo;
    private final StationRepository stationRepo;
    private final BookingRepository bookingRepo;
    private final EmailService emailService;

    public AdminService(TrainRepository trainRepo,
                        StationRepository stationRepo,
                        BookingRepository bookingRepo,
                        EmailService emailService) {
        this.trainRepo = trainRepo;
        this.stationRepo = stationRepo;
        this.bookingRepo = bookingRepo;
        this.emailService = emailService;
    }

    public List<Train> listTrains() {
        return trainRepo.findAll();
    }

    public List<Station> listStations() {
        return stationRepo.findAll();
    }

    @Transactional
    public Train createTrain(String trainCode, String name, int capacity, List<StopRequest> stops) {
        if (trainRepo.findByTrainCode(trainCode).isPresent()) {
            throw new IllegalArgumentException("Train already exists: " + trainCode);
        }

        Train train = new Train(trainCode, name, capacity);
        replaceStops(train, stops);
        return trainRepo.save(train);
    }

    @Transactional
    public Train updateTrain(String existingCode, String newCode, String name, int capacity, List<StopRequest> stops) {
        Train train = trainRepo.findByTrainCode(existingCode)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + existingCode));

        if (!existingCode.equals(newCode) && trainRepo.findByTrainCode(newCode).isPresent()) {
            throw new IllegalArgumentException("Another train already uses code: " + newCode);
        }

        train.setTrainCode(newCode);
        train.setName(name);
        train.setCapacity(capacity);

        replaceStops(train, stops);
        return trainRepo.save(train);
    }

    @Transactional
    public void deleteTrain(String trainCode) {
        Train train = trainRepo.findByTrainCode(trainCode)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainCode));

        List<Booking> bookings = bookingRepo.findByTrain(train);
        if (!bookings.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete train " + trainCode + " because it has existing bookings."
            );
        }

        trainRepo.delete(train);
    }

    @Transactional(readOnly = true)
    public List<Booking> bookingsForTrain(String trainCode) {
        Train train = trainRepo.findByTrainCode(trainCode)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainCode));
        return bookingRepo.findByTrainOrderByBookedAtDesc(train);
    }

    @Transactional
    public Station createStation(String code, String name) {
        if (stationRepo.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Station already exists: " + code);
        }
        return stationRepo.save(new Station(code, name));
    }

    @Transactional
    public Station updateStation(String code, String newCode, String name) {
        Station station = stationRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + code));

        if (!code.equals(newCode) && stationRepo.findByCode(newCode).isPresent()) {
            throw new IllegalArgumentException("Another station already uses code: " + newCode);
        }

        station.setCode(newCode);
        station.setName(name);
        return stationRepo.save(station);
    }

    @Transactional
    public void deleteStation(String code) {
        Station station = stationRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + code));

        for (Train train : trainRepo.findAll()) {
            for (StopTime stop : train.getStops()) {
                if (stop.getStation() != null && Objects.equals(stop.getStation().getId(), station.getId())) {
                    throw new IllegalStateException(
                            "Cannot delete station " + code + " because it is used in train routes."
                    );
                }
            }
        }

        stationRepo.delete(station);
    }

    @Transactional
    public int notifyDelay(String trainCode, int delayMinutes, String reason) {
        Train train = trainRepo.findByTrainCode(trainCode)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainCode));

        List<Booking> bookings = bookingRepo.findByTrain(train);
        Set<String> recipients = bookings.stream()
                .map(Booking::getPassengerEmail)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String subject = "Delay notice - train " + train.getTrainCode();
        String body = "Your train " + train.getTrainCode()
                + " is delayed by " + delayMinutes + " minutes."
                + (reason != null && !reason.isBlank() ? "\nReason: " + reason : "");

        for (String email : recipients) {
            emailService.send(email, subject, body);
        }

        return recipients.size();
    }

    private void replaceStops(Train train, List<StopRequest> stops) {
        train.getStops().clear();

        for (int i = 0; i < stops.size(); i++) {
            StopRequest req = stops.get(i);

            Station station = stationRepo.findByCode(req.stationCode)
                    .orElseThrow(() -> new IllegalArgumentException("Station not found: " + req.stationCode));

            StopTime stop = new StopTime(
                    train,
                    station,
                    i,
                    parseTime(req.arrival),
                    parseTime(req.departure)
            );
            train.getStops().add(stop);
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            return LocalTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    public static class StopRequest {
        public String stationCode;
        public String arrival;
        public String departure;
    }
}
