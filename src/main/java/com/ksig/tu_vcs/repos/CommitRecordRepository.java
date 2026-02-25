package com.ksig.tu_vcs.repos;


import com.ksig.tu_vcs.repos.entities.CommitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitRecordRepository extends JpaRepository<CommitRecord, Long> {
}