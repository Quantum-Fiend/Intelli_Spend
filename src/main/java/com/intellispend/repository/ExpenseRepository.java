package com.intellispend.repository;

import com.intellispend.entity.Expense;
import com.intellispend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    List<Expense> findAllByUser(User user);
    Page<Expense> findAllByUser(User user, Pageable pageable);
    List<Expense> findAllByUserId(Long userId);
}
