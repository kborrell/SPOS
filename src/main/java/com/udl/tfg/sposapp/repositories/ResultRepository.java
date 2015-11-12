package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.Result;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "result", path = "result")
public interface ResultRepository extends PagingAndSortingRepository<Result, Long> {
}
