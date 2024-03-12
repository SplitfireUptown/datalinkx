package com.datalinkx.dataserver.config.aop;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope
@Aspect
public class LogicDeleteAspect {
    static Map<CriteriaQuery<?>, Object> criteriaQueryGenForSpringDataJpaQueryMethod = new ConcurrentHashMap();

    public LogicDeleteAspect() {
    }


    @Around("execution(* javax.persistence.EntityManager.createQuery(..))")
    public Object createQuery(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args[0] instanceof String) {
            String jpql = String.valueOf(args[0]);
            if (args.length == 2) {
                Object arg2 = args[1];
                if (arg2 instanceof Class) {
                    Class<?> clazz = (Class)arg2;
                    if (!LogicDeleteAspect.class.isAssignableFrom(clazz)) {
                        return pjp.proceed();
                    }
                }
            }

            this.checkDeleteClause(jpql);
            if (jpql.contains("isDel")) {
                return pjp.proceed();
            } else {
                if (jpql.toUpperCase().contains("WHERE")) {
                    args[0] = jpql + " AND isDel=0";
                } else {
                    args[0] = jpql + " WHERE isDel=0";
                }

                return pjp.proceed(args);
            }
        } else {
            return pjp.proceed();
        }
    }

    @Around("execution(* javax.persistence.EntityManager.createNativeQuery(..))")
    public Object createNativeQuery(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        String nativeSQL    = String.valueOf(args[0]);
        if (args.length == 2) {
            Object arg2 = args[1];
            if (arg2 instanceof Class) {
                Class<?> clazz = (Class) arg2;
                if (!LogicDeleteAspect.class.isAssignableFrom(clazz)) {
                    return pjp.proceed();
                }
            }
        }

        this.checkDeleteClause(nativeSQL);
        if (nativeSQL.contains("is_del")) {
            return pjp.proceed();
        } else {
            if (nativeSQL.toUpperCase().contains("WHERE")) {
                args[0] = nativeSQL + " AND is_del=0";
            } else {
                args[0] = nativeSQL + " WHERE is_del=0";
            }

            return pjp.proceed(args);
        }
    }

    public void checkDeleteClause(String sql) {
        if (sql.toUpperCase().contains("DELETE")) {
            System.out.println(sql);
        }

    }
}
