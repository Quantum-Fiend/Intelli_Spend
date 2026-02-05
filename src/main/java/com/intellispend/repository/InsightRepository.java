package com.intellispend.repository;

import com.intellispend.entity.Insight;
import com.intellispend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface InsightRepository extends JpaRepository<Insight, Long> {
    Optional<Insight> findByUserAndMonth(User user, YearMonth month);
}
