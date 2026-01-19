Feature: Vehicle Charging
  As a customer
  I want to charge my electric vehicle
  So that I can use the charging network

  Background:
    Given the charging service is initialized

  Scenario: Start a charging session
    # UPDATED: Replaced € with EUR
    Given a customer with ID 1 exists with balance 50.00 EUR
    And a charging location with ID 1 exists with 1 charger
    And the specific charger has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    When the customer starts an AC charging session
    Then the charging session should be active
    And the specific charger should be occupied

  Scenario: Finish a charging session and calculate cost
    # UPDATED: Replaced € with EUR
    Given a customer with ID 1 exists with balance 50.00 EUR
    And a charging location with ID 1 exists with 1 charger
    And the specific charger has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    And the customer has an active charging session
    When the session finishes after 20 minutes with 12.5 kWh
    # UPDATED: Replaced € with EUR
    Then the total session cost should be 9.63 EUR
    # UPDATED: Replaced € with EUR
    And the customer balance should be 40.37 EUR
    And the specific charger should be available

  Scenario: Cannot start session without sufficient balance
    # UPDATED: Replaced € with EUR
    Given a customer with ID 1 exists with balance 0.50 EUR
    And a charging location with ID 1 exists with 1 charger

    # --- ADD THIS LINE ---
    And the specific charger has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    # ---------------------

    When the customer attempts to start a charging session
    Then the session should fail with error "Insufficient balance"

  Scenario: Cannot start session if charger is occupied
    # UPDATED: Replaced € with EUR
    Given a customer with ID 1 exists with balance 50.00 EUR
    # UPDATED: Replaced € with EUR
    And a customer with ID 2 exists with balance 50.00 EUR
    And a charging location with ID 1 exists with 1 charger

    # --- ADD THIS LINE ---
    And the specific charger has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    # ---------------------

    And customer 1 has an active session
    When customer 2 attempts to start a charging session
    Then the session start should fail