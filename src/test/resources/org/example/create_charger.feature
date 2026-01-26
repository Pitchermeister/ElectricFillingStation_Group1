Feature: Create Charger
  As a station operator
  I want to add chargers to locations
  So that customers can charge their vehicles

  Scenario: Add an AC charger to a location
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a "AC" charger with ID 101 and power 11.0 kW to location "City Center"
    Then the new charger should be registered in location "City Center"
    And the new charger should have ID 101
    And the new charger should have power 11.0 kW
    And the new charger status should be IN_OPERATION_FREE

  Scenario: Add a DC charger to a location
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a "DC" charger with ID 102 and power 150.0 kW to location "City Center"
    Then the new charger should be registered in location "City Center"
    And the new charger should have power 150.0 kW

  Scenario: Add multiple chargers to a location
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a specific "AC" charger ID 101 with 22.0 kW to location "City Center"
    And I add a specific "DC" charger ID 102 with 300.0 kW to location "City Center"
    Then location "City Center" should contain 2 chargers

  Scenario: Add charger with minimum AC power (edge case)
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a "AC" charger with ID 103 and power 2.3 kW to location "City Center"
    Then the new charger should be registered in location "City Center"
    And the new charger should have power 2.3 kW

  Scenario: Add charger with very high DC power (edge case)
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I add a "DC" charger with ID 104 and power 1000.0 kW to location "City Center"
    Then the new charger should be registered in location "City Center"
    And the new charger should have power 1000.0 kW

  Scenario: Attempt to add AC charger with too low power (error case)
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I attempt to add a "AC" charger with ID 105 and power 1.0 kW to location "City Center"
    Then the charger should not be registered
    And the system should show an error for invalid power

  Scenario: Attempt to add DC charger with too low power (error case)
    Given the station manager is initialized
    And a target location named "City Center" exists
    When I attempt to add a "DC" charger with ID 106 and power 20.0 kW to location "City Center"
    Then the charger should not be registered
    And the system should show an error for invalid power

  Scenario: Attempt to add charger to non-existent location (error case)
    Given the station manager is initialized
    # CHANGED WORDING: "confirming" instead of just "and" to avoid conflict
    And I confirm there is no location named "NonExistent"
    When I attempt to add a "AC" charger with ID 107 and power 11.0 kW to location "NonExistent"
    Then the charger should not be registered
    And the system should show an error for non-existent location