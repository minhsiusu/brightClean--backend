package com.example.brightClean.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.brightClean.domain.DeletedOrder;

public interface DeletedOrderRepository extends JpaRepository<DeletedOrder, Long> {

}
