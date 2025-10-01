package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();
        Ticket ticketSaved = ticketDAO.getTicket("ABCDEF");
        assertThat(ticketSaved).isNotNull();
        assertThat(ticketSaved.getInTime()).isNotNull();
        assertThat(ticketSaved.getOutTime()).isNull();
        assertThat(ticketSaved.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        ParkingSpot parkingSpot = ticketSaved.getParkingSpot();

        assertThat(parkingSpotDAO.getParkingAvailable(parkingSpot.getId())).isFalse();
    }

    @Test
    public void testParkingLotExit() throws InterruptedException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();
        Thread.sleep(1000);
        parkingService.processExitingVehicle();

        Ticket ticketUpdated = ticketDAO.getTicket("ABCDEF");
        assertThat(ticketUpdated.getOutTime()).isNotNull();
        assertThat(ticketUpdated.getOutTime()).isAfter(ticketUpdated.getInTime());
        assertThat(ticketUpdated.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        ParkingSpot parkingSpot = ticketUpdated.getParkingSpot();
        assertThat(parkingSpotDAO.getParkingAvailable(parkingSpot.getId())).isTrue();
    }

    @Test
    public void testParkingLotExitRecurringUser() throws InterruptedException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //First parking
        parkingService.processIncomingVehicle();
        //Simulate one hour parking
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        ticket.setInTime(oneHourAgo);
        ticketDAO.setInTimeTicket(ticket);
        parkingService.processExitingVehicle();

        //Second Parking
        parkingService.processIncomingVehicle();
        //Simulate one hour parking
        ticket = ticketDAO.getTicket("ABCDEF");
        oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        ticket.setInTime(oneHourAgo);
        ticketDAO.setInTimeTicket(ticket);
        parkingService.processExitingVehicle();

        ticket = ticketDAO.getTicket("ABCDEF");
        assertThat(ticket.getPrice()).isEqualByComparingTo(Fare.CAR_RATE_PER_HOUR.multiply(BigDecimal.valueOf(0.95)).setScale(2,RoundingMode.HALF_UP));
    }

}
