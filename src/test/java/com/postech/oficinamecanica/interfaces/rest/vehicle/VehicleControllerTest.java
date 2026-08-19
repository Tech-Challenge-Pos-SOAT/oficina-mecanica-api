package com.postech.oficinamecanica.interfaces.rest.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.oficinamecanica.application.vehicle.ChangeVehicleStatusCommand;
import com.postech.oficinamecanica.application.vehicle.ChangeVehicleStatusUseCase;
import com.postech.oficinamecanica.application.vehicle.CreateVehicleCommand;
import com.postech.oficinamecanica.application.vehicle.CreateVehicleUseCase;
import com.postech.oficinamecanica.application.vehicle.GetVehicleUseCase;
import com.postech.oficinamecanica.application.vehicle.ListVehiclesUseCase;
import com.postech.oficinamecanica.application.vehicle.UpdateVehicleCommand;
import com.postech.oficinamecanica.application.vehicle.UpdateVehicleUseCase;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.DuplicatePlateException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleAlreadyActiveException;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateVehicleUseCase createVehicleUseCase;

    @MockitoBean
    private UpdateVehicleUseCase updateVehicleUseCase;

    @MockitoBean
    private GetVehicleUseCase getVehicleUseCase;

    @MockitoBean
    private ListVehiclesUseCase listVehiclesUseCase;

    @MockitoBean
    private ChangeVehicleStatusUseCase changeVehicleStatusUseCase;

    @MockitoBean
    private VehicleRestMapper mapper;

    @Test
    void shouldReturnActiveVehiclesWhenNoStatusFilterProvided() throws Exception {
        Vehicle vehicle = aVehicle(EntityStatus.ACTIVE);
        VehicleResponse response = aResponse("ACTIVE");

        when(listVehiclesUseCase.execute(null)).thenReturn(List.of(vehicle));
        when(mapper.toResponse(vehicle)).thenReturn(response);

        mockMvc.perform(get("/api/vehicles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].plate").value("ABC-1234"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldReturnEmptyListWhenNoVehiclesMatchFilter() throws Exception {
        when(listVehiclesUseCase.execute(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/vehicles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldCreateVehicleAndReturn201() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(1L, "Toyota", "Corolla", "ABC-1234", 2021);
        Vehicle vehicle = aVehicle(EntityStatus.ACTIVE);
        VehicleResponse response = aResponse("ACTIVE");

        when(mapper.toCommand(request)).thenReturn(new CreateVehicleCommand(1L, "Toyota", "Corolla", "ABC-1234", 2021));
        when(createVehicleUseCase.execute(any())).thenReturn(vehicle);
        when(mapper.toResponse(vehicle)).thenReturn(response);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn400WhenCreateRequestHasBlankBrand() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(1L, "", "Corolla", "ABC-1234", 2021);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCreateRequestHasNullCustomerId() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(null, "Toyota", "Corolla", "ABC-1234", 2021);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenCreatingVehicleForNonExistentCustomer() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(99L, "Toyota", "Corolla", "ABC-1234", 2021);

        when(mapper.toCommand(request)).thenReturn(new CreateVehicleCommand(99L, "Toyota", "Corolla", "ABC-1234", 2021));
        when(createVehicleUseCase.execute(any())).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void shouldReturn409WhenCreatingVehicleForInactiveCustomer() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(1L, "Toyota", "Corolla", "ABC-1234", 2021);

        when(mapper.toCommand(request)).thenReturn(new CreateVehicleCommand(1L, "Toyota", "Corolla", "ABC-1234", 2021));
        when(createVehicleUseCase.execute(any())).thenThrow(new CustomerNotActiveException(1L));

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_ACTIVE"));
    }

    @Test
    void shouldReturn409WhenPlateAlreadyExists() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(1L, "Toyota", "Corolla", "ABC-1234", 2021);

        when(mapper.toCommand(request)).thenReturn(new CreateVehicleCommand(1L, "Toyota", "Corolla", "ABC-1234", 2021));
        when(createVehicleUseCase.execute(any()))
            .thenThrow(new DuplicatePlateException(new Plate("ABC-1234")));

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_PLATE"));
    }

    @Test
    void shouldUpdateVehicleAndReturn200() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Honda", "Civic", "XYZ-9876", 2022);
        Vehicle vehicle = aVehicle(EntityStatus.ACTIVE);
        VehicleResponse response = aResponse("ACTIVE");

        when(mapper.toCommand(1L, request)).thenReturn(new UpdateVehicleCommand(1L, "Honda", "Civic", "XYZ-9876", 2022));
        when(updateVehicleUseCase.execute(any())).thenReturn(vehicle);
        when(mapper.toResponse(vehicle)).thenReturn(response);

        mockMvc.perform(put("/api/vehicles/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenUpdatingVehicleThatDoesNotExist() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Honda", "Civic", "XYZ-9876", 2022);

        when(mapper.toCommand(99L, request)).thenReturn(new UpdateVehicleCommand(99L, "Honda", "Civic", "XYZ-9876", 2022));
        when(updateVehicleUseCase.execute(any())).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(put("/api/vehicles/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("VEHICLE_NOT_FOUND"));
    }

    @Test
    void shouldReturnVehicleByIdWhenFound() throws Exception {
        Vehicle vehicle = aVehicle(EntityStatus.ACTIVE);
        VehicleResponse response = aResponse("ACTIVE");

        when(getVehicleUseCase.execute(1L)).thenReturn(vehicle);
        when(mapper.toResponse(vehicle)).thenReturn(response);

        mockMvc.perform(get("/api/vehicles/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenVehicleByIdNotFound() throws Exception {
        when(getVehicleUseCase.execute(99L)).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(get("/api/vehicles/{id}", 99L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("VEHICLE_NOT_FOUND"));
    }

    @Test
    void shouldChangeVehicleStatusAndReturn200() throws Exception {
        ChangeVehicleStatusRequest request = new ChangeVehicleStatusRequest("INACTIVE");
        Vehicle vehicle = aVehicle(EntityStatus.INACTIVE);
        VehicleResponse response = aResponse("INACTIVE");

        when(mapper.toCommand(1L, request)).thenReturn(new ChangeVehicleStatusCommand(1L, "INACTIVE"));
        when(changeVehicleStatusUseCase.execute(any())).thenReturn(vehicle);
        when(mapper.toResponse(vehicle)).thenReturn(response);

        mockMvc.perform(patch("/api/vehicles/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldReturn409WhenReactivatingVehicleThatIsAlreadyActive() throws Exception {
        ChangeVehicleStatusRequest request = new ChangeVehicleStatusRequest("ACTIVE");

        when(mapper.toCommand(1L, request)).thenReturn(new ChangeVehicleStatusCommand(1L, "ACTIVE"));
        when(changeVehicleStatusUseCase.execute(any())).thenThrow(new VehicleAlreadyActiveException(1L));

        mockMvc.perform(patch("/api/vehicles/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("VEHICLE_ALREADY_ACTIVE"));
    }

    private static Vehicle aVehicle(EntityStatus status) {
        return new Vehicle(1L, 1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021,
            status, Instant.now(), Instant.now());
    }

    private static VehicleResponse aResponse(String status) {
        return new VehicleResponse(1L, 1L, "Toyota", "Corolla", "ABC-1234", 2021, status, Instant.now(), Instant.now());
    }
}
