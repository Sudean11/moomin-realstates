package com.academicproject.moomin.realstates.repo;

import com.academicproject.moomin.realstates.entity.Property;
import com.academicproject.moomin.realstates.entity.dtos.responseDto.PropertyFetchDTO;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface PropertyRepo extends JpaRepository<Property, Long> {

    List<Property> findAll(Specification<Property> specification);
    @Query("SELECT p FROM Property p ")
    List<Property> getFeaturedProperty();

    @Query("SELECT p FROM Property p join fetch p.location")
    @BatchSize(size = 5)
    List<Property> getTest();


    @Query("SELECT p FROM Property p ORDER BY p.id DESC")
    List<Property> findLast8AddedProperties(Pageable pageable);


}
