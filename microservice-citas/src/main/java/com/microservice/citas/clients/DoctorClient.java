package com.microservice.citas.clients;

import com.microservice.citas.http.response.doctor.DetalleDoctorDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "mcsv-users", path = "/api/doctor")
public interface DoctorClient {

    @GetMapping("/find_by_id/{id_Doctor}")
    DetalleDoctorDto findById(@PathVariable UUID id_Doctor);
}
