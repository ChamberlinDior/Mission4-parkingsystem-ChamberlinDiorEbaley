package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	//Objectif : Ce test vérifie si le calcul du tarif d'une voiture est correct.
	//Scénario : il crée un Ticketobjet avec un inTime défini sur une heure auparavant,
	// un outTime défini sur l'heure actuelle et une place de parking pour une voiture.
	// Il calcule ensuite le tarif fareCalculatorService.calculateFareet affirme que le prix calculé correspond à la valeur attendue.
	@Test
	public void calculateFareCar() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT);
		assertThat(ticket.getPrice()).isEqualTo((1 - 0.5) * Fare.CAR_RATE_PER_HOUR);
	}

	//Objectif : Ce test vérifie si le calcul du tarif d'un vélo est correct.
	//Scénario : Similaire au calculateFareCartest, mais pour une place de stationnement vélo.
	@Test
	public void calculateFareBike() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT);
		assertThat(ticket.getPrice()).isEqualTo((1 - 0.5) * Fare.BIKE_RATE_PER_HOUR);
	}

	// Objectif : Ce test vérifie si le système gère correctement un type de stationnement inconnu.
	//Scénario : il crée un Ticketobjet avec un inTime et un outTime définis il y a une heure et une place
	// de stationnement avec un type de stationnement nul. Il affirme ensuite que l'appel
	@Test
	public void calculateFareUnkownType() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT));
	}

	//Objectif : Ce test vérifie si le système gère correctement un vélo avec un futur inTime.
	//Scénario : il crée un Ticketobjet avec un inTime défini sur une heure dans le futur, un outTime défini sur
	// l'heure actuelle et une place de stationnement pour un vélo. Il affirme que l'appel fareCalculatorService.calculateFareà ce ticket génère un IllegalArgumentException.
	@Test
	public void calculateFareBikeWithFutureInTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class,
				() -> fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT));
	}

	//Objectif : Ce test vérifie si le calcul du tarif pour un vélo avec un temps de stationnement inférieur à une heure est correct.
	//Scénario : il crée un Ticketobjet avec un inTime défini sur 45 minutes auparavant, un outTime défini sur l'heure actuelle et une place de stationnement pour un vélo.
	// Il calcule ensuite le tarif et affirme que le prix calculé correspond à la valeur attendue.
	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
		//  (45 minutes - 30  minutes gratuit)

		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT);
		assertThat(ticket.getPrice()).isEqualTo((0.75 - 0.5) * Fare.BIKE_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
		// 50 minutes parking time should give 1/4th parking fare (50 minutes - 30
		// minutes free)
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT);
		assertThat(ticket.getPrice()).isEqualTo((0.75 - 0.5) * Fare.CAR_RATE_PER_HOUR);

	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
		// 24 hours parking time should give 23.5 * parking fare per hour (24 hours - 30
		// minutes free)
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT);
		assertThat(ticket.getPrice()).isEqualTo((24 - 0.5) * Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareCarWithLessThanHalfAnHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (15 * 60 * 1000));
		// 15 minutes parking time should give 0 * parking fare per hour (30 minutes
		// free)
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, Fare.NO_DISCOUNT);
		assertThat(ticket.getPrice()).isEqualTo(0);

	}

}
