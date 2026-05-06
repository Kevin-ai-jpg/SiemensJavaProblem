package repo;

import model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {
    Optional<Train> findByTrainCode(String trainCode);

    // For booking we fetch the train with a PESSIMISTIC_WRITE lock to serialize seat allocation
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select t from Train t where t.id = :id")
//    Optional<Train> findByIdForUpdate(Long id);
}
