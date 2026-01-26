Feature: Vehicle Charging
  As a customer
  I want to charge my electric vehicle
  So that I can use the charging network

  Background:
    Given the charging service is initialized

  Scenario: Start a charging session
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    When the charging customer "Alice" starts an "AC" charging session on charger 101 at "City Center"
    Then the charging session should be active
    And charger 101 should be OCCUPIED

  Scenario: Finish a charging session and calculate cost
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the session finishes after 20 minutes with 12.5 kWh
    Then the total session cost should be 9.63 EUR
    And the charging customer "Alice" balance should be 40.37 EUR
    And charger 101 should be AVAILABLE
    And the invoice for "Alice" should contain the following entry:
      | Location    | AC_or_DC | Start Time | End Time | Energy   | Cost     |
      | City Center | AC       | RECENT     | RECENT   | 12.5 kWh | 9.63 EUR |

  Scenario: Verify Charging Network History (Owner Report)
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging customer "Bob" exists with balance 100.00 EUR
    And a charging customer "Charlie" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And a charging location named "Airport" exists with 2 chargers
    And a charging location named "Highway" exists with 2 chargers

    # Alice charges at City Center (Charger 101 is AC by default logic)
    When the charging customer "Alice" starts an "AC" charging session on charger 101 at "City Center"
    And the session finishes after 20 minutes with 12.5 kWh

    # Bob charges at Airport (Charger 102 is DC by default logic)
    When the charging customer "Bob" starts a "DC" charging session on charger 102 at "Airport"
    And the session finishes after 30 minutes with 40.0 kWh

    # Charlie charges at Highway (Charger 102 is DC by default logic)
    When the charging customer "Charlie" starts a "DC" charging session on charger 102 at "Highway"
    And the session finishes after 15 minutes with 25.0 kWh

    Then the system should have the following charging entries:
      | client  | location    | charger | mode | time   | kWh  | cost  |
      | Alice   | City Center | 101     | AC   | 20 min | 12.5 | 9.63  |
      | Bob     | Airport     | 102     | DC   | 30 min | 40.0 | 32.00 |
      | Charlie | Highway     | 102     | DC   | 15 min | 25.0 | 19.25 |

  Scenario: Cannot start session without sufficient balance
    Given a charging customer "Alice" exists with balance 0.50 EUR
    And a charging location named "City Center" exists with 1 chargers
    When the charging customer "Alice" attempts to start a charging session on charger 101 at "City Center"
    Then the session should fail with error "Insufficient balance"

  Scenario: Cannot start session if charger is occupied
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging customer "Bob" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the charging customer "Bob" attempts to start a charging session on charger 101 at "City Center"
    Then the session start should fail

  Scenario: Charging session with very short duration (edge case)
    Given a charging customer "Alice" exists with balance 50.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the session finishes after 1 minute with 0.1 kWh
    Then the total session cost should be greater than 0
    And the charging customer "Alice" balance should decrease

  Scenario: Session with very high energy consumption (edge case)
    Given a charging customer "Alice" exists with balance 500.00 EUR
    And a charging location named "City Center" exists with 1 chargers
    And the charging customer "Alice" has an active session on charger 101 at "City Center"
    When the session finishes after 100 minutes with 250.0 kWh
    Then the total session cost should be calculated correctly
    And the charging customer "Alice" balance should decrease