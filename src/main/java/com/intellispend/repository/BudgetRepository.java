package com.intellispend.repository;

import com.intellispend.entity.Budget;
import com.intellispend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByUser(User user);
    Optional<Budget> findByUserAndCategoryAndMonth(User user, String category, YearMonth month);
}
