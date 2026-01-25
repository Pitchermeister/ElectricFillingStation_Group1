Feature: Create Client
  As a system administrator
  I want to register new clients
  So that they can use the charging network

  Scenario: Register a new client
    # UPDATED LINE:
    Given the client management system is initialized
    When I register a client with name "John Doe" and email "john@example.com"
    Then the client should be saved in the system
    And the client should have name "John Doe"
    And the client should have email "john@example.com"
    And the client should have zero balance

  Scenario: Register multiple clients
    # UPDATED LINE:
    Given the client management system is initialized
    When I register client "Alice" with email "alice@test.com"
    And I register client "Bob" with email "bob@test.com"
    Then the system should have 2 clients