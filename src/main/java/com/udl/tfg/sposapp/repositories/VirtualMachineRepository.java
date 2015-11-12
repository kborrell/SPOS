package com.udl.tfg.sposapp.repositories;

import com.udl.tfg.sposapp.models.VirtualMachine;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "virtualmachine", path = "virtualmachine")
public interface VirtualMachineRepository extends PagingAndSortingRepository<VirtualMachine, Long> {

}
