package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {


	public void calculateFare(Ticket ticket, double discount) {


		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		Long inTime = ticket.getInTime().getTime();
		Long outTime = ticket.getOutTime().getTime();

		// TODO: Some tests are failing here. Need to check if this logic is correct
		// Fixed
		double duration = ((double) (outTime - inTime)) / (60 * 60 * 1000);

		// 30 minutes gratuite
		if (duration < 0.5) {
			duration = 0;
		} else {
			duration -= 0.5;
		}

		// 5% de reduction pour les users regulier
		duration *= discount;

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
			break;
		}
		case BIKE: {
			ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
			break;
		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");

			//Cette instruction de commutation vérifie le type de stationnement donné Ticket et calcule
			// le prix en conséquence. S'il s'agit d'un CAR, il multiplie la durée par CAR_RATE_PER_HOUR et
			// définit le résultat comme prix du billet. Si c'est un BIKE, il fait la même chose avec le BIKE_RATE_PER_HOUR.
			// Si le type de stationnement est inconnu, il renvoie un IllegalArgumentExceptionmessage d'erreur
		}
	}
}