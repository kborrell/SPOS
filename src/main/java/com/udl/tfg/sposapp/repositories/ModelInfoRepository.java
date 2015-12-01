package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.ModelInfo;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "models", path = "models")
public interface ModelInfoRepository extends PagingAndSortingRepository<ModelInfo, Long> {

}
