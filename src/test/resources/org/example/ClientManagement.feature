Feature: Client Management
  As an Administrator
  I want to register new clients
  So that they can use the charging network

  # Scenario for: Create Client
  Scenario: Successfully register a new client
    Given I have a client name "Max Mustermann" and email "maxm@example.com"
    When I register the new client
    Then the client should be saved in the system
    And the system should return a valid Client ID