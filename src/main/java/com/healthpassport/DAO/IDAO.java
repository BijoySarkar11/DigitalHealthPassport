package com.healthpassport.MODEL.dao;

import java.util.List;

// Uses Java Generics <T> so it can work with Patient, Doctor, or Admin
public interface IDAO<T> {
    boolean create(T entity);
    boolean update(T entity);
    boolean delete(String systemId);
    T findBySystemId(String systemId);
    List<T> findAll();
}