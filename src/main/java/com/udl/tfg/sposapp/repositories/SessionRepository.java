package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.Session;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "session", path = "session")
public interface SessionRepository extends PagingAndSortingRepository<Session, Long> {
}
