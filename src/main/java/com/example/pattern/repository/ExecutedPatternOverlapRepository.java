package com.example.pattern.repository;

import com.example.pattern.model.ExecutedPatternOverlap;
import com.example.pattern.model.ExecutedPatternOverlapId;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutedPatternOverlapRepository extends JpaRepositoryImplementation<ExecutedPatternOverlap, ExecutedPatternOverlapId> {
}
