Feature: Create Location
  As a station operator
  I want to create charging locations
  So that I can manage my network

  Scenario: Create a new location
    Given the system is initialized
    When I create a location with ID 1, name "City Center" and address "Main Street 1"
    Then the location should be saved
    And the location should have ID 1
    And the location should have name "City Center"
    And the location should have address "Main Street 1"

  Scenario: Create multiple locations
    Given the system is initialized
    When I create location ID 1 "Downtown" at "1st Avenue"
    And I create location ID 2 "Airport" at "Terminal Road"
    Then the system should have 2 locations
