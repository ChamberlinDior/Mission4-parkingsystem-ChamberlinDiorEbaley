package com.parkit.parkingsystem.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

public class ParkingService {

	private static final Logger logger = LogManager.getLogger("ParkingService");

	private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

	private InputReaderUtil inputReaderUtil;
	private ParkingSpotDAO parkingSpotDAO;
	private TicketDAO ticketDAO;

	public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
		this.inputReaderUtil = inputReaderUtil;
		this.parkingSpotDAO = parkingSpotDAO;
		this.ticketDAO = ticketDAO;
	}

	public void processIncomingVehicle(Date entryTime) {
		try {
			ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
			if (parkingSpot != null && parkingSpot.getId() > 0) {
				String vehicleRegNumber = getVehichleRegNumber();

				// Welcome back message
				if (ticketDAO.isRecurringUser(vehicleRegNumber)) {
					System.out.println(
							"Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
				}

				parkingSpot.setAvailable(false);

				// allot this parking space and mark it's availability as false
				parkingSpotDAO.updateParking(parkingSpot);

				Date inTime = entryTime;
				Ticket ticket = new Ticket();
				// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
				// ticket.setId(ticketID);
				ticket.setParkingSpot(parkingSpot);
				ticket.setVehicleRegNumber(vehicleRegNumber);
				ticket.setPrice(0);
				ticket.setInTime(inTime);
				ticket.setOutTime(null);
				ticketDAO.saveTicket(ticket);
				System.out.println("Generated Ticket and saved in DB");
				System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
				System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
			}
		} catch (Exception e) {
			logger.error("Unable to process incoming vehicle", e);
		}
	}

	private String getVehichleRegNumber() throws Exception {
		System.out.println("Please type the vehicle registration number and press enter key");
		return inputReaderUtil.readVehicleRegistrationNumber();
	}

	public ParkingSpot getNextParkingNumberIfAvailable() {
		int parkingNumber = 0;
		ParkingSpot parkingSpot = null;
		try {
			ParkingType parkingType = getVehichleType();
			parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
			if (parkingNumber > 0) {
				parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
			} else {
				throw new Exception("Error fetching parking number from DB. Parking slots might be full");
			}
		} catch (IllegalArgumentException ie) {
			logger.error("Error parsing user input for type of vehicle", ie);
		} catch (Exception e) {
			logger.error("Error fetching next available parking slot", e);
		}
		return parkingSpot;
	}

	private ParkingType getVehichleType() {
		System.out.println("Please select vehicle type from menu");
		System.out.println("1 CAR");
		System.out.println("2 BIKE");
		int input = inputReaderUtil.readSelection();
		switch (input) {
		case 1: {
			return ParkingType.CAR;
		}
		case 2: {
			return ParkingType.BIKE;
		}
		default: {
			System.out.println("Incorrect input provided");
			throw new IllegalArgumentException("Entered input is invalid");
		}
		}
	}

	public void processExitingVehicle(Date entryTime) {
		try {
			//Demande du numéro d'immatriculation du véhicule
			String vehicleRegNumber = getVehichleRegNumber();
			// Récupération du ticket associé au numéro d'immatriculation du véhicule
			Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
			double discount = Fare.NO_DISCOUNT;
			Date outTime = entryTime;
			// Définir l'heure de sortie pour le ticket
			ticket.setOutTime(outTime);

			// Appliquer une remise de 5% pour les véhicules récurrents
			if (ticketDAO.isRecurringUser(vehicleRegNumber)) {
				discount = Fare.FIVE_PERCENT_DISCOUNT;
			}
			// Calculer le tarif pour le ticket
			fareCalculatorService.calculateFare(ticket, discount);
			// Mettre à jour les informations du ticket dans la base de données
			if (ticketDAO.updateTicket(ticket)) {
				ParkingSpot parkingSpot = ticket.getParkingSpot();
				// Définir la place de parking comme disponible
				parkingSpot.setAvailable(true);
				parkingSpotDAO.updateParking(parkingSpot);
				// Afficher le tarif de stationnement à payer et l'heure de sortie enregistrée
				System.out.println("Please pay the parking fare:" + ticket.getPrice());
				// Afficher un message d'erreur en cas d'incapacité à mettre à jour les informations du ticket
				System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
			} else {
				System.out.println("Unable to update ticket information. Error occurred");
			}
		} catch (Exception e) {
			// Journaliser un message d'erreur si une exception se produit pendant le processus
			logger.error("Unable to process exiting vehicle", e);
		}
	}
}
