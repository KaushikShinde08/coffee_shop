package com.example.coffee_shop.repository;

import com.example.coffee_shop.model.Barista;
import com.example.coffee_shop.model.BaristaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaristaRepository extends JpaRepository<Barista, Long> {
    List<Barista> findByStatus(BaristaStatus status);
}
