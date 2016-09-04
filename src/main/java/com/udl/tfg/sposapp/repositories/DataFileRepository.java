package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.DataFile;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "dataFiles", path = "dataFiles")
public interface DataFileRepository extends PagingAndSortingRepository<DataFile, Long> {

}
