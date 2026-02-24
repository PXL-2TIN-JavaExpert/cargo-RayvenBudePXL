package be.pxl.cargo.api;

import be.pxl.cargo.api.request.CreateCargoRequest;
import be.pxl.cargo.api.response.CargoStatistics;
import be.pxl.cargo.domain.CargoStatus;
import be.pxl.cargo.domain.Location;
import be.pxl.cargo.service.CargoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CargoController.class)
public class CargoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CargoService cargoService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addCargo_ShouldReturnCreated() throws Exception{
        CreateCargoRequest request = new CreateCargoRequest("CARGO_1",200.0,Location.AIRPORT_X,Location.CITY_B);
        mockMvc.perform(post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void addCargo_weightTooLow_returns400() throws Exception{
        mockMvc.perform(post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "code": "CARGO_1",
                        "weight": 50,
                        "origin": "AIRPORT_X",
                        "destination": "CITY_B"
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCargo_nullDestination_returns400() throws Exception{
        mockMvc.perform(post("/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "CARGO_1",
                        "weight": 350,
                        "origin": "AIRPORT_X",
                        "destination": null
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCargo_codeBlank_returns400() throws Exception{
        mockMvc.perform(post("/cargos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "",
                        "weight": 350,
                        "origin": "AIRPORT_X",
                        "destination": "CITY_B"
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCargo_nullOrigin_return400() throws Exception{

        CreateCargoRequest request=
                new CreateCargoRequest(
                        "CARGO_1",
                        200.0,
                        null,
                        Location.CITY_B
                );
        mockMvc.perform(post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
//        mockMvc.perform(post("/cargos")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                    {
//                        "code": "CARGO_1",
//                        "weight": 50,
//                        "destination": "CITY_B"
//                    }
//                """))
//                .andExpect(status().isBadRequest());
    }

    @Test
    void  getStatistics_returns200AndBody() throws Exception{
        CargoStatistics stats = new CargoStatistics();
        stats.setStatusCount(Map.of(CargoStatus.CREATED,2L));
        stats.setHeaviestCargo("HEAVY");
        stats.setAverageCargoWeight(350.0);
        stats.setCountCargosAtWarehouseA(1);
        stats.setTotalWeightDeliveredAtCityB(500.0);
        when(cargoService.getCargoStatistics()).thenReturn(stats);

        mockMvc.perform(get("/cargos/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heaviestCargo").value("HEAVY"))
                .andExpect(jsonPath("$.averageCargoWeight").value(350.0))
                .andExpect(jsonPath("$.countCargosAtWarehouseA").value(1))
                .andExpect(jsonPath("$.totalWeightDeliveredAtCityB").value(500.0))
                .andExpect(jsonPath("$.statusCount.CREATED").value(2));
    }



}
