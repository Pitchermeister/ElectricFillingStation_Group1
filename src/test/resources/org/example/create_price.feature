Feature: Create Price
  As a station operator
  I want to set prices for charging
  So that I can bill customers correctly

  Scenario: Set initial pricing for a location
    Given the pricing service is initialized
    And a pricing location named "City Center" exists with 1 charger
    When I set pricing for location "City Center": AC 0.45 EUR per kWh, DC 0.65 EUR per kWh, 0.20 EUR per min
    Then location "City Center" should have AC price 0.45 EUR per kWh
    And location "City Center" should have DC price 0.65 EUR per kWh
    And location "City Center" pricing should apply to all chargers
    And location "City Center" pricing should have a timestamp

  Scenario: Different locations have different prices
    Given the pricing service is initialized
    And a pricing location named "City Center" exists with 1 charger
    And a pricing location named "Airport" exists with 1 charger
    When I set location "City Center" pricing: AC 0.40 EUR per kWh
    And I set location "Airport" pricing: AC 0.50 EUR per kWh
    Then location "City Center" should have AC price 0.40 EUR per kWh
    And location "Airport" should have AC price 0.50 EUR per kWh
    And location "City Center" pricing should have a timestamp
    And location "Airport" pricing should have a timestamp

  Scenario: Update pricing multiple times for same location (edge case)
    Given the pricing service is initialized
    And a pricing location named "City Center" exists with 1 charger
    When I set location "City Center" pricing: AC 0.40 EUR per kWh
    And I set location "City Center" pricing: AC 0.60 EUR per kWh
    Then location "City Center" should have AC price 0.60 EUR per kWh
    # ADDED: Timestamp check
    And location "City Center" pricing should have a timestamp

  Scenario: Set pricing with zero AC rate (edge case)
    Given the pricing service is initialized
    And a pricing location named "Test Location" exists with 1 charger
    When I set pricing for location "Test Location": AC 0.00 EUR per kWh, DC 0.65 EUR per kWh, 0.20 EUR per min
    Then location "Test Location" should have AC price 0.00 EUR per kWh
    And location "Test Location" should have DC price 0.65 EUR per kWh
    # ADDED: Timestamp check
    And location "Test Location" pricing should have a timestamp

  Scenario: Set pricing with very high DC rate (edge case)
    Given the pricing service is initialized
    And a pricing location named "Premium Location" exists with 1 charger
    When I set pricing for location "Premium Location": AC 0.45 EUR per kWh, DC 5.99 EUR per kWh, 0.20 EUR per min
    Then location "Premium Location" should have DC price 5.99 EUR per kWh
    # ADDED: Timestamp check
    And location "Premium Location" pricing should have a timestamp

  Scenario: Attempt to set pricing for non-existent location (error case)
    Given the pricing service is initialized
    And there is no location with the name "NonExistent"
    When I attempt to set pricing for location "NonExistent": AC 0.45 EUR per kWh, DC 0.65 EUR per kWh, 0.20 EUR per min
    Then an error should be returned for non-existent location