package me.oldboy.core.model.database.repository;

import me.oldboy.core.model.database.audit.Audit;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import org.hibernate.SessionFactory;

public class AuditRepository extends RepositoryBase<Long, Audit> {
    public AuditRepository(SessionFactory sessionFactory) {
        super(Audit.class, sessionFactory);
    }
}
