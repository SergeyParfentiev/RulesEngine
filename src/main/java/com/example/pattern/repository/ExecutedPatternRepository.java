package com.example.pattern.repository;

import com.example.pattern.model.ExecutedPattern;
import com.example.pattern.model.ExecutedPatternId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutedPatternRepository extends JpaRepositoryImplementation<ExecutedPattern, ExecutedPatternId>,
        JpaSpecificationExecutor<ExecutedPattern> {
}
