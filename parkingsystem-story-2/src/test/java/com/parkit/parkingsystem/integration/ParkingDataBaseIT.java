package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
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
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		// to verify that ticket not already exist
		Ticket ticketBeforeTest = ticketDAO.getTicket("ABCDEF");
		assertThat(ticketBeforeTest).isNull();

		parkingService.processIncomingVehicle(new Date());
		// TODO: check that a ticket is actualy saved in DB and Parking table is updated
		// with availability
		Ticket ticketTest = ticketDAO.getTicket("ABCDEF");
		assertThat(ticketTest).isNotNull();
		assertThat(ticketTest.getParkingSpot().isAvailable()).isFalse();
	}

	@Test
	public void testParkingLotExit() {

		// inTime one hour before
		Date inTime = new Date();
		Date outTime = new Date();
		// inTime.setTime(System.currentTimeMillis());
		inTime.setTime(0);
		outTime.setTime(60 * 60 * 1000);

		// testParkingACar();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle(inTime);

		// to verify that outTime is null and price is equal to 0
		Ticket ticketBeforeTest = ticketDAO.getTicket("ABCDEF");
		assertThat(ticketBeforeTest.getOutTime()).isNull();
		assertThat(ticketBeforeTest.getPrice()).isEqualTo(0);

		parkingService.processExitingVehicle(outTime);
		// TODO: check that the fare generated and out time are populated correctly in
		// the database
		Ticket ticketTest = ticketDAO.getTicket("ABCDEF");
		assertThat(ticketTest.getOutTime()).isNotNull();
		assertThat(ticketTest.getPrice()).isEqualTo(0.75);
	}

	@Test
	public void testDiscountForRecurringUsers() {

		// Simulation : Car "ABCDEF" park at 0 Time, exit 1 hour after,
		// park again 2 hours after and exit again 3 hours after
		Date firstInTime = new Date();
		Date firstOutTime = new Date();
		Date secondInTime = new Date();
		Date secondOutTime = new Date();

		// firstInTime.setTime(System.currentTimeMillis());
		firstInTime.setTime(0);
		firstOutTime.setTime(1 * 60 * 60 * 1000);
		secondInTime.setTime(2 * 60 * 60 * 1000);
		secondOutTime.setTime(3 * 60 * 60 * 1000);

		//
		ParkingService parkingFirstService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingFirstService.processIncomingVehicle(firstInTime);
		parkingFirstService.processExitingVehicle(firstOutTime);

		Ticket firstTicketTest = ticketDAO.getTicket("ABCDEF");
		// check that the fare for a CAR during 1 hour is correctly calculated
		assertThat(firstTicketTest.getPrice()).isEqualTo(0.75);

		ParkingService parkingSecondService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingSecondService.processIncomingVehicle(secondInTime);
		parkingSecondService.processExitingVehicle(secondOutTime);

		Ticket secondTicketTest = ticketDAO.getTicket("ABCDEF");

		// TODO: check that the fare generated is for 1 hour reduced by 5%
		assertThat(secondTicketTest.getPrice()).isEqualTo(0.75 * Fare.FIVE_PERCENT_DISCOUNT);
	}

}
