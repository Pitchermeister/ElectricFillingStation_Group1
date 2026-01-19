Feature: Vehicle Charging
  As a customer
  I want to charge my electric vehicle
  So that I can use the charging network

  Background:
    Given the system is initialized

  Scenario: Start a charging session
    Given a client with ID 1 exists with balance €50.00
    And a location with ID 1 exists with 1 charger
    And the charger has pricing AC €0.45/kWh and €0.20/min
    When the client starts an AC charging session
    Then the session should be active
    And the charger should be occupied

  Scenario: Finish a charging session and calculate cost
    Given a client with ID 1 exists with balance €50.00
    And a location with ID 1 exists with 1 charger
    And the charger has pricing AC €0.45/kWh and €0.20/min
    And the client has an active charging session
    When the session finishes after 20 minutes with 12.5 kWh
    Then the total cost should be €9.63
    And the client balance should be €40.37
    And the charger should be available

  Scenario: Cannot start session without sufficient balance
    Given a client with ID 1 exists with balance €0.50
    And a location with ID 1 exists with 1 charger
    When the client attempts to start a charging session
    Then the session should fail with "Insufficient balance"

  Scenario: Cannot start session if charger is occupied
    Given a client with ID 1 exists with balance €50.00
    And a client with ID 2 exists with balance €50.00
    And a location with ID 1 exists with 1 charger
    And client 1 has an active session
    When client 2 attempts to start a charging session
    Then the session should fail
