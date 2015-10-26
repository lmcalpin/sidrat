package com.sidrat.event.store.jpa;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.sidrat.event.store.jpa.model.BaseSidratEntity;
import com.sidrat.event.store.jpa.model.SidratEvent;
import com.sidrat.event.store.jpa.model.SidratValueObject;

/**
 * Utility class for dealing with JPA.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class JPADAO {
    @FunctionalInterface
    private interface Session<T> {
        public T apply(EntityManager em);
    }

    EntityManagerFactory emf;
    private AtomicLong generatedId = new AtomicLong();
    private String partition;

    public JPADAO(String partition) {
        this.emf = Persistence.createEntityManagerFactory("sidrat");
        this.partition = partition;
    }

    private <T extends BaseSidratEntity> List<T> executePartitionedQuery(String jpaql, Map<String, Object> parameters, EntityManager em) {
        return executePartitionedQuery(jpaql, parameters, em, -1);
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseSidratEntity> List<T> executePartitionedQuery(String jpaql, Map<String, Object> parameters, EntityManager em, int limit) {
        // todo: criteria api?
        if (jpaql.toUpperCase().contains(" WHERE ")) {
            int indexOfWhere = jpaql.toUpperCase().indexOf(" WHERE ");
            StringBuilder newJpaQl = new StringBuilder();
            newJpaQl.append(jpaql.substring(0, indexOfWhere));
            newJpaQl.append(" WHERE partition = :partition AND ");
            newJpaQl.append(jpaql.substring(indexOfWhere + " WHERE ".length()));
            jpaql = newJpaQl.toString();
        } else {
            jpaql += " WHERE partition = :partition";
        }
        Query query = em.createQuery(jpaql);
        if (limit > -1)
            query.setMaxResults(limit);
        for (String key : parameters.keySet()) {
            query.setParameter(key, parameters.get(key));
        }
        query.setParameter("partition", partition);
        List<T> results = query.getResultList();
        return results;
    }

    public <T extends BaseSidratEntity> List<T> find(String jpaql, Map<String, Object> parameters) {
        return findSession(em -> {
            return executePartitionedQuery(jpaql, parameters, em);
        });
    }

    public <T extends SidratEvent> T findByTime(Class<T> clazz, Long time) {
        Map<String, Object> params = new HashMap<>();
        params.put("time", time);
        return findSingle("FROM " + clazz.getSimpleName() + " WHERE time = :time", params);
    }

    public <T extends BaseSidratEntity> T findFirst(String jpaql) {
        return findSingle(jpaql, Collections.emptyMap());
    }

    public <T extends BaseSidratEntity> T findFirst(String jpaql, Map<String, Object> parameters) {
        return findSession(em -> {
            List<T> results = executePartitionedQuery(jpaql, parameters, em);
            if (results.isEmpty())
                return null;
            return results.get(0);
        });
    }

    public <T extends SidratValueObject> T findOrCreate(T named) {
        return findSession(em -> {
            List<T> results = executePartitionedQuery("FROM " + named.getClass().getSimpleName() + " WHERE name = :name", Collections.singletonMap("name", named.getName()), em, 2);
            if (results.isEmpty()) {
                return store(named);
            } else if (results.size() == 1) {
                return results.get(0);
            }
            throw new IllegalStateException("Too many results found for " + named.getClass().getSimpleName() + " named '" + named.getName() + "'");
        });
    }

    private <T> T findSession(Session<T> f) {
        EntityManager em = emf.createEntityManager();
        try {
            T results = f.apply(em);
            return results;
        } finally {
            em.close();
        }
    }

    public <T extends BaseSidratEntity> T findSingle(String jpaql) {
        return findSingle(jpaql, Collections.emptyMap());
    }

    public <T extends BaseSidratEntity> T findSingle(String jpaql, Map<String, Object> parameters) {
        return findSession(em -> {
            List<T> results = executePartitionedQuery(jpaql, parameters, em, 2);
            if (results.isEmpty())
                return null;
            if (results.size() > 1)
                throw new IllegalStateException("Too many results for: " + jpaql);
            return results.get(0);
        });
    }

    public <T extends BaseSidratEntity> void persist(T entity) {
        entity.setPartition(partition);
        updateSession(em -> {
            em.persist(entity);
            return null;
        });
    }

    public <T extends BaseSidratEntity> T store(T entity) {
        entity.setPartition(partition);
        return updateSession(em -> em.merge(entity));
    }

    private <T> T updateSession(Session<T> f) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        T t = null;
        try {
            tx.begin();
            t = f.apply(em);
            em.flush();
            tx.commit();
        } finally {
            em.close();
        }
        return t;
    }
}
