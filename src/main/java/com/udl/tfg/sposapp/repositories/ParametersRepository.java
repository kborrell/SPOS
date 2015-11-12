package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.Parameters;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "parameters", path = "parameters")
public interface ParametersRepository extends PagingAndSortingRepository<Parameters, Long> {
}
