package be.pxl.cargo.service;

import be.pxl.cargo.api.request.CreateCargoRequest;
import be.pxl.cargo.api.response.CargoStatistics;
import be.pxl.cargo.domain.Cargo;
import be.pxl.cargo.domain.CargoStatus;
import be.pxl.cargo.domain.Location;
import be.pxl.cargo.exceptions.NonUniqueCodeException;
import be.pxl.cargo.repository.CargoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @InjectMocks
    private CargoService cargoService;

    @Test
    void createCargo_ShouldSaveCargo_whenCodeUnique() {
        CreateCargoRequest request = new CreateCargoRequest("Electronics", 200.0, Location.WAREHOUSE_A, Location.CITY_B);
        when(cargoRepository.findCargoByCode("Electronics")).thenReturn(Optional.empty());
        cargoService.createCargo(request);

        verify(cargoRepository, times(1)).save(any(Cargo.class));
    }

    @Test
    void createCargo_shouldThrowException_whenCodeNotUnique(){
        CreateCargoRequest request = new CreateCargoRequest("Electronics", 200.0, Location.WAREHOUSE_A, Location.CITY_B);
        when(cargoRepository.findCargoByCode("Electronics"))
                .thenReturn(Optional.of(mock(Cargo.class)));
        assertThrows(NonUniqueCodeException.class,
                () -> cargoService.createCargo(request));
    }

    @Test
    void getCargoStatistics_shouldReturnCorrectStatistics() {

        Cargo cargo1 = new Cargo("C1", 100.0,
                Location.WAREHOUSE_A, Location.CITY_B);
        cargo1.arrive(Location.CITY_B);

        Cargo cargo2 = new Cargo("C2", 300.0,
                Location.WAREHOUSE_A, Location.CITY_B);

        when(cargoRepository.findAll())
                .thenReturn(List.of(cargo1, cargo2));

        CargoStatistics stats = cargoService.getCargoStatistics();

        assertEquals("C2", stats.getHeaviestCargo());
        assertEquals(200.0, stats.getAverageCargoWeight());
        assertEquals(1L, stats.getCountCargosAtWarehouseA());
        assertEquals(100.0, stats.getTotalWeightDeliveredAtCityB());
        assertEquals(1L, stats.getStatusCount().get(CargoStatus.CREATED));
        assertEquals(1L, stats.getStatusCount().get(CargoStatus.DELIVERED));
    }

}
