package service;

import model.*;
import repo.StationRepository;
import repo.TrainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/*
  Implements:
  - admin operations (add/remove trains, add stations)
  - search direct journeys and single-transfer journeys
*/
@Service
public class TrainService {
    private final TrainRepository trainRepo;
    private final StationRepository stationRepo;

    public TrainService(TrainRepository trainRepo, StationRepository stationRepo) {
        this.trainRepo = trainRepo; this.stationRepo = stationRepo;
    }

    @Transactional
    public Train createTrain(String code, String name, int capacity, List<StopTime> stops) {
        Train t = new Train(code, name, capacity);
        // set train reference in StopTimes
        for (int i = 0; i < stops.size(); i++) {
            StopTime st = stops.get(i);
            st = new StopTime(t, st.getStation(), i, st.getArrival(), st.getDeparture());
            t.getStops().add(st);
        }
        return trainRepo.save(t);
    }

    public List<Train> listTrains() {
        return trainRepo.findAll();
    }

    public Optional<Train> findByCode(String code) {
        return trainRepo.findByTrainCode(code);
    }

    @Transactional(readOnly = true)
    public List<JourneyDto> findDirect(String fromCode, String toCode) {
        List<JourneyDto> result = new ArrayList<>();
        for (Train t : trainRepo.findAll()) {
            List<StopTime> stops = t.getStops();
            Integer fromIdx = null, toIdx = null;
            for (StopTime st : stops) {
                if (st.getStation().getCode().equals(fromCode)) fromIdx = st.getStopIndex();
                if (st.getStation().getCode().equals(toCode)) toIdx = st.getStopIndex();
            }
            if (fromIdx != null && toIdx != null && fromIdx < toIdx) {
                JourneyDto.Leg leg = new JourneyDto.Leg(t.getTrainCode(),
                        stops.get(fromIdx).getStation().getCode(), stops.get(toIdx).getStation().getCode(),
                        stops.get(fromIdx).getDeparture(), stops.get(toIdx).getArrival(), t.getCapacity());
                result.add(new JourneyDto(List.of(leg)));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<JourneyDto> findSingleTransfer(String fromCode, String toCode, int minTransferMin) {
        List<JourneyDto> result = new ArrayList<>();
        List<Train> trains = trainRepo.findAll();
        for (Train t1 : trains) {
            for (Train t2 : trains) {
                // find common transfer station
                for (StopTime st1 : t1.getStops()) {
                    String transfer = st1.getStation().getCode();
                    // must be after fromCode on t1 and before toCode on t2
                    OptionalInt fromIdxOpt = t1.getStops().stream()
                            .filter(s -> s.getStation().getCode().equals(fromCode))
                            .mapToInt(StopTime::getStopIndex).findFirst();
                    OptionalInt transferIdx1Opt = t1.getStops().stream()
                            .filter(s -> s.getStation().getCode().equals(transfer))
                            .mapToInt(StopTime::getStopIndex).findFirst();
                    OptionalInt transferIdx2Opt = t2.getStops().stream()
                            .filter(s -> s.getStation().getCode().equals(transfer))
                            .mapToInt(StopTime::getStopIndex).findFirst();
                    OptionalInt toIdxOpt = t2.getStops().stream()
                            .filter(s -> s.getStation().getCode().equals(toCode))
                            .mapToInt(StopTime::getStopIndex).findFirst();

                    if (fromIdxOpt.isEmpty() || transferIdx1Opt.isEmpty() || transferIdx2Opt.isEmpty() || toIdxOpt.isEmpty())
                        continue;
                    int fromIdx = fromIdxOpt.getAsInt();
                    int t1Transfer = transferIdx1Opt.getAsInt();
                    int t2Transfer = transferIdx2Opt.getAsInt();
                    int toIdx = toIdxOpt.getAsInt();

                    if (!(fromIdx < t1Transfer && t2Transfer < toIdx)) continue;

                    LocalTime arrivalAtTransfer = t1.getStops().get(t1Transfer).getArrival();
                    LocalTime departSecond = t2.getStops().get(t2Transfer).getDeparture();
                    if (arrivalAtTransfer == null || departSecond == null) continue;
                    if (!departSecond.isAfter(arrivalAtTransfer.plusMinutes(minTransferMin - 1))) continue;

                    JourneyDto.Leg leg1 = new JourneyDto.Leg(t1.getTrainCode(),
                            t1.getStops().get(fromIdx).getStation().getCode(),
                            t1.getStops().get(t1Transfer).getStation().getCode(),
                            t1.getStops().get(fromIdx).getDeparture(), arrivalAtTransfer, t1.getCapacity());

                    JourneyDto.Leg leg2 = new JourneyDto.Leg(t2.getTrainCode(),
                            t2.getStops().get(t2Transfer).getStation().getCode(),
                            t2.getStops().get(toIdx).getStation().getCode(),
                            departSecond, t2.getStops().get(toIdx).getArrival(), t2.getCapacity());

                    result.add(new JourneyDto(List.of(leg1, leg2)));
                }
            }
        }
        // distinct by toString
        return result.stream().distinct().collect(Collectors.toList());
    }
}
