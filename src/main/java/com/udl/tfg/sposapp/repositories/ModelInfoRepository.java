package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.MethodCodes;
import com.udl.tfg.sposapp.models.ModelCodes;
import com.udl.tfg.sposapp.models.ModelInfo;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "models", path = "models")
public interface ModelInfoRepository extends PagingAndSortingRepository<ModelInfo, Long> {

    List<ModelInfo> findByModel(@Param("modelName") ModelCodes model);
}
