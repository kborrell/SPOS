package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.MethodCodes;
import com.udl.tfg.sposapp.models.MethodInfo;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "methods", path = "methods")
public interface MethodInfoRepository extends PagingAndSortingRepository<MethodInfo, Long> {

    List<MethodCodes> findByMethod(@Param("modelName") MethodCodes model);
}
