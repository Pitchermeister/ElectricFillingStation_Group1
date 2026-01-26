Feature: Create Location
  As a station owner
  I want to create new locations
  So that I can assign chargers to them

  Scenario: Create a new location successfully
    Given the location service is initialized
    When I create a location with name "City Center" and address "Main St 1"
    Then the location should be saved
    And the location should have name "City Center"
    And the location should have address "Main St 1"

  Scenario: Create multiple locations
    Given the location service is initialized
    When I create the following locations:
      | Name      | Address  |
      | West Side | West Ave |
      | East Side | East Ave |
    Then the system should have 2 locations

  Scenario: Create location with minimal address length (edge case)
    Given the location service is initialized
    When I create a location with name "Hub" and address "A"
    Then the location should be saved
    And the location should have name "Hub"
    And the location should have address "A"

  Scenario: Create location with very long name (edge case)
    Given the location service is initialized
    When I create a location with name "The Very Important International Electric Vehicle Charging Hub With Extended Facilities" and address "Main Street"
    Then the location should be saved
    And the location should have name "The Very Important International Electric Vehicle Charging Hub With Extended Facilities"

  Scenario: Create location with duplicate name (edge case)
    Given the location service is initialized
    When I create location "City Center" at "Main St"
    And I create location "City Center" at "Different Address"
    Then the system should have 2 locations

  Scenario: Create multiple locations sequentially (edge case)
    Given the location service is initialized
    When I create the following locations:
      | Name | Address |
      | Loc1 | Addr1   |
      | Loc2 | Addr2   |
      | Loc3 | Addr3   |
      | Loc4 | Addr4   |
      | Loc5 | Addr5   |
    Then the system should have 5 locations

  # NEW: Error Case
  Scenario: Attempt to create location with empty name (error case)
    Given the location service is initialized
    When I attempt to create a location with name "" and address "Main St"
    Then the location should not be created
    And an error should be returned for invalid location data