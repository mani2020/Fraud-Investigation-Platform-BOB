package com.fraud.platform.repository;

import com.fraud.platform.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transaction by transaction ID.
     */
    Optional<Transaction> findByTxnId(String txnId);

    /**
     * Find all transactions for a customer.
     */
    List<Transaction> findByCustomerIdOrderByTimestampDesc(String customerId);

    /**
     * Find transactions for a customer within a time range.
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY t.timestamp DESC")
    List<Transaction> findByCustomerIdAndTimestampBetween(
        @Param("customerId") String customerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Count transactions for a customer within a time range.
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.timestamp BETWEEN :startTime AND :endTime")
    Long countByCustomerIdAndTimestampBetween(
        @Param("customerId") String customerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find transactions by status.
     */
    List<Transaction> findByStatusOrderByTimestampDesc(String status);

    /**
     * Find transactions by fraud decision.
     */
    List<Transaction> findByFraudDecisionOrderByTimestampDesc(String fraudDecision);

    /**
     * Calculate average transaction amount for a customer within a time range.
     */
    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.timestamp BETWEEN :startTime AND :endTime")
    BigDecimal calculateAverageAmountByCustomerIdAndTimestampBetween(
        @Param("customerId") String customerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Check if a device has been used by a customer before.
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.deviceId = :deviceId AND t.timestamp < :beforeTime")
    boolean existsByCustomerIdAndDeviceIdBeforeTime(
        @Param("customerId") String customerId,
        @Param("deviceId") String deviceId,
        @Param("beforeTime") LocalDateTime beforeTime
    );

    /**
     * Count distinct customers using a device.
     */
    @Query("SELECT COUNT(DISTINCT t.customerId) FROM Transaction t WHERE t.deviceId = :deviceId")
    Long countDistinctCustomersByDeviceId(@Param("deviceId") String deviceId);
}

// Made with Bob
