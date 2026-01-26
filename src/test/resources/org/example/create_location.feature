Feature: Create Location
  As a station owner
  I want to create new locations
  So that I can assign chargers to them

  # Scenario 1
  Scenario: Create a new location successfully
    # UPDATED LINE BELOW:
    Given the location service is initialized
    When I create a location with name "City Center" and address "Main St 1"
    Then the location should be saved
    And the location should have name "City Center"
    And the location should have address "Main St 1"

  # Scenario 2
  Scenario: Create multiple locations
    # UPDATED LINE BELOW:
    Given the location service is initialized
    When I create location "West Side" at "West Ave"
    And I create location "East Side" at "East Ave"
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
    When I create location "Loc1" at "Addr1"
    And I create location "Loc2" at "Addr2"
    And I create location "Loc3" at "Addr3"
    And I create location "Loc4" at "Addr4"
    And I create location "Loc5" at "Addr5"
    Then the system should have 5 locations