package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.MethodInfo;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "methods", path = "methods")
public interface MethodInfoRepository extends PagingAndSortingRepository<MethodInfo, Long> {
}
