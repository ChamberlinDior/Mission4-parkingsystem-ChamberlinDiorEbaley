package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	// private TicketDAO ticketDAO;

	public void calculateFare(Ticket ticket, double discount) {

		// ticketDAO = null;

		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		Long inTime = ticket.getInTime().getTime();
		Long outTime = ticket.getOutTime().getTime();

		// TODO: Some tests are failing here. Need to check if this logic is correct
		// Fixed
		double duration = ((double) (outTime - inTime)) / (60 * 60 * 1000);

		// 30 minutes free
		if (duration < 0.5) {
			duration = 0;
		} else {
			duration -= 0.5;
		}

		// 5% discount for recurring user
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
		}
	}
}