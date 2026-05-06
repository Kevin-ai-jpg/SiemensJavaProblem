package controller;

import model.Booking;
import model.Station;
import model.Train;
import service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/trains")
    public List<TrainDto> trains() {
        return adminService.listTrains().stream().map(TrainDto::from).toList();
    }

    @GetMapping("/stations")
    public List<StationDto> stations() {
        return adminService.listStations().stream().map(StationDto::from).toList();
    }

    @PostMapping("/trains")
    public TrainDto createTrain(@RequestBody TrainRequest request) {
        return TrainDto.from(
                adminService.createTrain(request.trainCode, request.name, request.capacity, request.stops)
        );
    }

    @PutMapping("/trains/{trainCode}")
    public TrainDto updateTrain(@PathVariable String trainCode, @RequestBody TrainRequest request) {
        return TrainDto.from(
                adminService.updateTrain(trainCode, request.trainCode, request.name, request.capacity, request.stops)
        );
    }

    @DeleteMapping("/trains/{trainCode}")
    public String deleteTrain(@PathVariable String trainCode) {
        adminService.deleteTrain(trainCode);
        return "Deleted train " + trainCode;
    }

    @GetMapping("/trains/{trainCode}/bookings")
    public List<BookingDto> bookings(@PathVariable String trainCode) {
        return adminService.bookingsForTrain(trainCode).stream().map(BookingDto::from).toList();
    }

    @PostMapping("/trains/{trainCode}/delay")
    public String delay(@PathVariable String trainCode, @RequestBody DelayRequest request) {
        int sent = adminService.notifyDelay(trainCode, request.delayMinutes, request.reason);
        return "Sent delay notification to " + sent + " passengers";
    }

    @PostMapping("/stations")
    public StationDto createStation(@RequestBody StationRequest request) {
        return StationDto.from(adminService.createStation(request.code, request.name));
    }

    @PutMapping("/stations/{code}")
    public StationDto updateStation(@PathVariable String code, @RequestBody StationRequest request) {
        return StationDto.from(adminService.updateStation(code, request.code, request.name));
    }

    @DeleteMapping("/stations/{code}")
    public String deleteStation(@PathVariable String code) {
        adminService.deleteStation(code);
        return "Deleted station " + code;
    }

    public static class TrainRequest {
        public String trainCode;
        public String name;
        public int capacity;
        public List<service.AdminService.StopRequest> stops;
    }

    public static class StationRequest {
        public String code;
        public String name;
    }

    public static class DelayRequest {
        public int delayMinutes;
        public String reason;
    }

    public static class StationDto {
        public Long id;
        public String code;
        public String name;

        static StationDto from(Station s) {
            StationDto dto = new StationDto();
            dto.id = s.getId();
            dto.code = s.getCode();
            dto.name = s.getName();
            return dto;
        }
    }

    public static class StopDto {
        public Long id;
        public String stationCode;
        public String stationName;
        public int stopIndex;
        public LocalTime arrival;
        public LocalTime departure;
    }

    public static class TrainDto {
        public Long id;
        public String trainCode;
        public String name;
        public int capacity;
        public List<StopDto> stops;

        static TrainDto from(Train train) {
            TrainDto dto = new TrainDto();
            dto.id = train.getId();
            dto.trainCode = train.getTrainCode();
            dto.name = train.getName();
            dto.capacity = train.getCapacity();
            dto.stops = train.getStops().stream().map(stop -> {
                StopDto sd = new StopDto();
                sd.id = stop.getId();
                sd.stationCode = stop.getStation().getCode();
                sd.stationName = stop.getStation().getName();
                sd.stopIndex = stop.getStopIndex();
                sd.arrival = stop.getArrival();
                sd.departure = stop.getDeparture();
                return sd;
            }).toList();
            return dto;
        }
    }

    public static class BookingDto {
        public Long id;
        public String trainCode;
        public String passengerName;
        public String passengerEmail;
        public int fromIndex;
        public int toIndex;
        public int seats;
        public LocalDateTime bookedAt;

        static BookingDto from(Booking booking) {
            BookingDto dto = new BookingDto();
            dto.id = booking.getId();
            dto.trainCode = booking.getTrain().getTrainCode();
            dto.passengerName = booking.getPassengerName();
            dto.passengerEmail = booking.getPassengerEmail();
            dto.fromIndex = booking.getFromIndex();
            dto.toIndex = booking.getToIndex();
            dto.seats = booking.getSeats();
            dto.bookedAt = booking.getBookedAt();
            return dto;
        }
    }
}
