# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

Key Factors to Consider:

Data Sources & Integration: The first step is to identify where the labor, transportation, and inventory data currently resides and determine how we can access it (e.g., via APIs, database connections, or file exports).
Cost Allocation Model: The most complex part is often not tracking direct costs, but agreeing on the business rules to allocate shared costs (like warehouse rent or management salaries) across different products, stores, or shipments.
Data Granularity: We need to clarify the level of detail required. Are we tracking costs per-order, per-shipment, or per-store? This decision is fundamental to the data model.
Potential Challenges:

Data Quality: The biggest technical risk is "garbage in, garbage out." I foresee a significant effort in cleaning, validating, and standardizing data from multiple source systems.
Complex Business Logic: Translating the rules for allocating indirect costs into a robust and fair technical model is difficult and often requires navigating political considerations.
Scope Creep: There will be a temptation to track everything at once. It's critical to maintain a tight focus on the initial MVP to ensure the project doesn't become too complex to deliver in a timely manner.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

Enforce Mandatory Fields: Reject any request with missing critical data, such as the businessUnitCode.
Guarantee Uniqueness: Prevent the creation of a warehouse if a record with the same businessUnitCode already exists.
Validate Business Rules: Implement checks for complex domain rules, such as ensuring a warehouse's stated capacity is permissible for its location.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
Integrating with financial systems is critical for creating a single source of truth for cost data. This eliminates manual data entry, reduces errors, and provides real-time visibility into profitability, which is essential for accurate financial reporting and strategic decision-making.

To ensure seamless integration, I would use an event-driven architecture, leveraging message queues for real-time, asynchronous data synchronization. This approach decouples the systems, improves resilience, and ensures that robust monitoring and alerting can be put in place to immediately detect and address any data sync failures.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Unit Tests: To verify individual business rules in isolation, such as the warehouse capacity and location validation logic shown in the test files.
Integration Tests: To ensure that the service layer interacts correctly with the database, for instance, by preventing the creation of duplicate businessUnitCode entries.
End-to-End API Tests: To validate the entire workflow from the API request down to the database, confirming that the system behaves as expected from a user's perspective.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
Defining Technical Debt: Clearly categorizing debt into types like bugs, security vulnerabilities, or necessary refactoring.
Prioritization Framework: Using a framework that balances the effort required to fix the debt against its business impact (e.g., risk of failure, development slowdown).
Dedicated Allocation: Advocating for a dedicated percentage of each sprint's capacity (e.g., 15-20%) to be allocated specifically to addressing prioritized technical debt.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
