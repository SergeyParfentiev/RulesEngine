package com.example.pattern.repository;

import com.example.pattern.model.Pattern;
import com.example.pattern.model.PatternId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatternRepository extends JpaRepository<Pattern, PatternId>, JpaSpecificationExecutor<Pattern> {
}
