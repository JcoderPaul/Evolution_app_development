package me.oldboy.repository;

import me.oldboy.models.audit.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long>, CrudRepository<Audit, Long> {

}
