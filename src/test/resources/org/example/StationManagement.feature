Feature: Station and Charger Management
  As an Owner
  I want to manage locations and chargers
  So that the network is up to date

  # Scenario for: Create Location
  Scenario: Create a new charging location
    Given I have a location details "Main Street Garage" and zip code "1010"
    When I create the location
    Then the location should be listed in the system

  # Scenario for: Create Charger (and create price implicitly or explicitly)
  Scenario: Add a new charger to a location
    Given a location exists with the name "Main Street Garage"
    And I have a charger with ID "CH-01" and type "DC Fast"
    And the price per kWh is set to 0.50 EUR
    When I add the charger to the location
    Then the charger "CH-01" should be associated with "Main Street Garage"

  # Scenario for: Read Charger Information
  Scenario: Read charger details
    Given a charger "CH-01" exists in the system
    When I request information for charger "CH-01"
    Then I should receive the type "DC Fast"
    And I should see the price 0.50 EUR