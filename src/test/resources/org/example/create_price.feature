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
    #nutzlose information
    And a pricing location named "Airport" exists with 1 charger
    When I set location "City Center" pricing: AC 0.40 EUR per kWh
    And I set location "Airport" pricing: AC 0.50 EUR per kWh
    Then location "City Center" should have AC price 0.40 EUR per kWh
    And location "Airport" should have AC price 0.50 EUR per kWh
    And location "City Center" pricing should have a timestamp
    And location "Airport" pricing should have a timestamp


    # Datum und Uhrzeit fehlt -> denke es ist erledigt mit pricing should have timestamp