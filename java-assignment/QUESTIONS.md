# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```

I would refactor all repositories to follow the pattern used by WarehouseRepository, where a repository implements an interface (e.g., ProductStore, StoreStore). This clearly defines the contract for data access and decouples the rest of the application from the specific database implementation (Panache in this case).

However, I would also introduce a mapping framework (like MapStruct) to handle the conversion between entities (like DbWarehouse) and DTOs (like WarehouseDTO). Manual mapping, as seen in WarehouseRepository, is tedious and error-prone. A mapping framework would automate this, reducing boilerplate code and potential bugs.

Benefits of this refactoring:

Consistency: All repositories would follow the same pattern, making the code easier to understand and maintain.
Separation of Concerns: The data access logic would be clearly separated from the business logic, improving modularity and testability.
Reduced Boilerplate: A mapping framework would eliminate the need for manual DTO-entity mapping code.
Improved Testability: With a clear interface, it becomes easier to mock the repository for unit testing the services that use it.

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```

This approach, also known as "design-first," involves writing the API specification (the warehouse-openapi.yaml file) before writing any implementation code. Code generation tools then create server stubs and client SDKs from this specification.

Pros:

Clear Contract: The OpenAPI specification acts as a single source of truth. It's a clear, language-agnostic contract that both frontend and backend developers can agree on before any code is written.
Parallel Development: With the contract in place, frontend and backend teams can work in parallel. The frontend team can use the spec to create mock servers and build UI components, confident that the backend will match the specification.
Strong Tooling Ecosystem: A wide range of tools can use the OpenAPI spec to automatically generate server skeletons, client libraries in various languages, API documentation, and even test cases. This saves significant development time.
Enforced Consistency: It encourages a consistent API design across the entire application because all changes must be deliberately made in the specification file.
Cons:

Initial Overhead: There is a learning curve and an initial time investment in writing the YAML/JSON specification file. For very simple APIs, this might feel like overkill.
Tooling Dependency: You are reliant on the code generation tools. The generated code might not always be as clean or idiomatic as hand-written code, and you might need to work around the tool's limitations.
Synchronization Effort: The design and implementation are in two different places. Keeping the OpenAPI spec and the backend implementation in perfect sync requires discipline and can sometimes complicate the development workflow.
This approach involves writing the endpoint handler code directly. The API specification, if needed, is often generated from the code and its annotations (e.g., JAX-RS annotations in Java).

Pros:

Development Speed: For developers, this is often the fastest way to get started. They can work directly in their preferred programming language and framework without needing to learn the OpenAPI syntax.
Flexibility: It's very easy to make changes and iterate quickly. There's no separate design file to update, which can make prototyping faster.
Single Source of Truth (in Code): The implementation itself is the source of truth, which eliminates any possibility of the code and the specification drifting out of sync.
Cons:

Delayed Contract: The concrete API contract isn't available until the code is written and, potentially, the specification is generated. This can delay or complicate frontend or inter-service integration.
Risk of Inconsistent API Design: Without a separate design phase, it's easier for inconsistencies to creep into the API design, especially across different teams or a large number of endpoints.
Documentation as an Afterthought: API documentation can be neglected. While many frameworks can generate a spec from code, it's only as good as the annotations and comments the developer provides.
For a project of this nature, my choice would be the API-first approach.

Justification:

The "API-first" approach provides a more robust and scalable foundation for a system with multiple entities (Warehouse, Store, Product). The benefits of having a clear, upfront contract outweigh the initial overhead. This contract is crucial for:

Team Collaboration: It enables parallel work streams, which is essential for efficient development in a team setting.
Long-term Maintainability: It enforces consistency and provides clear documentation, making the API easier to understand, consume, and maintain over time.
Scalability: As new services or consumers are added, the clear contract defined by the OpenAPI spec makes integration much simpler and less error-prone.
While the code-first approach offers speed for initial development, it often leads to challenges in the long run. I would recommend refactoring the Product and Store endpoints to follow the same API-first pattern used by the Warehouse API to ensure consistency and maintainability across the entire project.

```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```

I would adopt the "Testing Pyramid" model as a guiding principle. This model prioritizes having a large base of fast, isolated unit tests, a smaller number of integration tests, and a very small number of slow, end-to-end tests.

What they are: These tests verify the smallest pieces of your code, like a single method or class, in isolation from their dependencies.
Why they are a priority: They are fast to write and run, providing immediate feedback to developers. They allow you to test all the edge cases of your business logic without the overhead of spinning up databases or external services.
Implementation for this project:
Target: The highest priority for unit tests is the domain logic within the usecases packages (e.g., CreateWarehouseUseCase, ArchiveWarehouseUseCase). The existing tests for these use cases are a perfect example of this focus.
How: I would use mocking frameworks like Mockito to isolate the class under test. For example, when testing CreateWarehouseUseCase, I would mock the WarehouseStore and LocationResolver interfaces to simulate their behavior and verify that the use case calls them correctly.
What they are: These tests verify the interaction between different components of the system.
Why they are a priority: They catch errors that unit tests can't, such as database schema mismatches, incorrect query logic, or issues with external service communication.
Implementation for this project:
Target 1 (Database Interaction): Test the repository layer (e.g., WarehouseRepository, ProductRepository) against a real, but temporary, test database. The existing WarehouseEndpointIT.java which uses @QuarkusTest is a good example of an integration test that covers the API down to the database. I would ensure every repository has similar integration tests to validate query logic and data integrity rules (like uniqueness constraints).
Target 2 (External Services): Test the gateway implementations (LocationGateway, LegacyStoreManagerGateway). We can use tools like WireMock to simulate the external APIs and verify that our gateways handle successful responses and error conditions correctly. The existing LegacyStoreManagerGatewayTest.java demonstrates this principle.
What they are: These tests validate entire user workflows from the outside, by making real HTTP requests to the running application and asserting the results.
Why they are a priority: They provide the highest confidence that the system works as a whole. However, they are also the slowest and most brittle, so they should be used sparingly.
Implementation for this project:
Target: Focus on the "happy path" of the most critical user journeys. For example:
Create a new warehouse.
Retrieve that warehouse to confirm it was created.
Update the warehouse.
Archive the warehouse and confirm it no longer appears in the "get all" list.
How: The existing @QuarkusTest annotations in files like WarehouseEndpointIT.java and ProductEndpointTest.java already provide the framework for these tests. I would group these critical workflow tests into a separate suite that runs less frequently than the unit and integration tests (e.g., only on builds to a staging environment).
Having a good testing strategy isn't a one-time setup; it requires continuous effort.

Automate Everything: All tests (unit, integration, and E2E) must be integrated into the CI/CD pipeline. The build must fail if any test fails. This is non-negotiable.
Enforce Test-Driven Practices in PRs: Implement a policy for Pull Requests that requires:
New features or bug fixes must include corresponding tests.
All tests must pass before a PR can be merged.
Use a code coverage tool (like JaCoCo) to measure test coverage and set a reasonable threshold (e.g., 80% line coverage). The key is not to hit a specific number, but to prevent the coverage from decreasing over time.
Regularly Review and Refactor Tests: Treat your test code like production code. If a test is slow, brittle, or hard to understand, refactor it. A test suite that is not maintained will quickly become a liability rather than an asset.
Focus on Criticality: When time is limited, prioritize testing based on the complexity and business impact of the code. A complex pricing calculation deserves more thorough testing than a simple getter or setter.

```
