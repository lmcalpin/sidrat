package com.sidrat.event.store.jpa;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.sidrat.event.store.jpa.model.BaseSidratEntity;

public class JPADAO {
    @FunctionalInterface
    private interface FindSession<T> {
        public T apply(EntityManager em);
    }

    @FunctionalInterface
    private interface UpdateSession {
        public void apply(EntityManager em);
    }

    EntityManagerFactory emf;
    private AtomicLong generatedId = new AtomicLong();

    public JPADAO(String partition) {
        emf = Persistence.createEntityManagerFactory("sidrat");
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseSidratEntity> List<T> find(String jpaql, Map<String, Object> parameters) {
        return findSession(em -> {
            Query query = em.createQuery(jpaql);
            for (String key : parameters.keySet()) {
                query.setParameter(key, parameters.get(key));
            }
            return query.getResultList();
        });
    }

    public <T extends BaseSidratEntity> T findFirst(String jpaql) {
        return findSingle(jpaql, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseSidratEntity> T findFirst(String jpaql, Map<String, Object> parameters) {
        return findSession(em -> {
            Query query = em.createQuery(jpaql);
            query.setMaxResults(1);
            for (String key : parameters.keySet()) {
                query.setParameter(key, parameters.get(key));
            }
            List<T> results = query.getResultList();
            if (results.isEmpty())
                return null;
            return results.get(0);
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseSidratEntity & Named> T findOrCreate(T named) {
        return findSession(em -> {
            Query query = em.createQuery("FROM " + named.getClass().getSimpleName() + " WHERE name = :name");
            query.setParameter("name", named.getName());
            List<T> results = query.getResultList();
            if (results.isEmpty()) {
                named.setId(generatedId.incrementAndGet());
                return named;
            } else if (results.size() == 1) {
                return results.get(0);
            }
            throw new IllegalStateException("Too many results found for " + named.getClass().getSimpleName() + " named '" + named.getName() + "'");
        });
    }

    private <T> T findSession(FindSession<T> f) {
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

    @SuppressWarnings("unchecked")
    public <T extends BaseSidratEntity> T findSingle(String jpaql, Map<String, Object> parameters) {
        return findSession(em -> {
            Query query = em.createQuery(jpaql);
            query.setMaxResults(2);
            for (String key : parameters.keySet()) {
                query.setParameter(key, parameters.get(key));
            }
            List<T> results = query.getResultList();
            if (results.isEmpty())
                return null;
            if (results.size() > 1)
                throw new IllegalStateException("Too many results for: " + jpaql);
            return results.get(0);
        });
    }

    public <T extends BaseSidratEntity> void persist(T entity) {
        updateSession(em -> em.merge(entity));
    }

    private void updateSession(UpdateSession f) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            f.apply(em);
            em.flush();
            tx.commit();
        } finally {
            em.close();
        }
    }
}
