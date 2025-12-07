package com.example.group_service.data.repositories;

import com.example.group_service.data.entities.GroupOrder;
import com.example.group_service.data.enums.GroupStatus;
import feign.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GroupOrder g where g.id = :id")
    Optional<GroupOrder> findByIdForUpdate(@Param("id") Long id);

    List<GroupOrder> findByStatusInAndExpiresAtBefore(List<GroupStatus> statuses, Instant expiresAt);
}
