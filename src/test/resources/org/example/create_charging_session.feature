Feature: Vehicle Charging
  As a customer
  I want to charge my electric vehicle
  So that I can use the charging network

  Background:
    Given the charging service is initialized

  Scenario: Start a charging session
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
        #auch unnötig
    When the charging customer "Alice" starts an AC charging session on charger 101 at "City Center"
    Then the charging session should be active
    And charger 101 should be OCCUPIED

  Scenario: Finish a charging session and calculate cost
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the session finishes after 20 minutes with 12.5 kWh
    Then the total session cost should be 9.63 EUR
    And the charging customer "Alice" balance should be 40.37 EUR
    And charger 101 should be AVAILABLE
    #Rechnung erstellen

  Scenario: Cannot start session without sufficient balance
    Given a charging customer "Alice" exists with balance 0.50 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    When the charging customer "Alice" attempts to start a charging session on charger 101 at "City Center"
    Then the session should fail with error "Insufficient balance"

  Scenario: Cannot start session if charger is occupied
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging customer "Bob" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the charging customer "Bob" attempts to start a charging session on charger 101 at "City Center"
    Then the session start should fail

  Scenario: Charging session with very short duration (edge case)
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the session finishes after 1 minute with 0.1 kWh
    Then the total session cost should be greater than 0
    And the charging customer "Alice" balance should decrease

  Scenario: Charging session with multiple chargers in same location (edge case)
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging customer "Bob" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 2 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    And the charger with ID 102 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    When the charging customer "Alice" starts an AC charging session on charger 101 at "City Center"
    And the charging customer "Bob" starts an AC charging session on charger 102 at "City Center"
    Then both charging sessions should be active
    And charger 101 should be OCCUPIED
    And charger 102 should be OCCUPIED

  Scenario: Cannot start session with exactly zero balance (error case)
    Given a charging customer "Alice" exists with balance 0.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    When the charging customer "Alice" attempts to start a charging session on charger 101 at "City Center"
    Then the session should fail with error "Insufficient balance"

  Scenario: Session with very high energy consumption (edge case)
    Given a charging customer "Alice" exists with balance 500.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charger with ID 101 has pricing AC 0.45 EUR per kWh and 0.20 EUR per min
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the session finishes after 100 minutes with 250.0 kWh
    Then the total session cost should be calculated correctly
    And the charging customer "Alice" balance should decrease
