### 0: Setup
- ~~create noaa app~~
- ~~create db~~
- ~~Create dummy lead table with test data~~
- ~~Generate dummy test data~~
- ~~Create lead noaa table~~
- ~~connect to noaa db~~
- ~~Define noaa cutoff date env var~~

### 1: Mark leads eligible for NOAA generation
- ~~Identify leads requiring noaas (rejection code of 623, no noaa previously created and lead created >= noaa cutoff date~~
- ~~Create noaa records for eligible leads~~
- ~~Build a driver function to identify leads needing noaas and generate noaa records for them.~~


### 2: Generate text for a NOAA
- ~~Define a template - No visine~~
- ~~Load data for template merge from lead~~
- ~~Visine retrieval protocol~~
- ~~Implement dummy visine report~~
- ~~Load empty visine report through dummy protocol~~
- ~~Create letter from template and lead data~~
- ~~Write letter to noaa table~~
- ~~Update noaa gen attributes in noaa table~~
- ~~Write a template - Visine report present~~
- ~~choose correct template to use~~
- ~~fill in visine template~~
- ~~write a driver to generate noaas for all leads flagged in the identification step.~~
- ~~Make it easy to set up lots of testing data.~~
- ~~Handle processing exceptions for the identification pass gracefully.~~


### 3:  Deliver a NOAA
- ~~Load generated noaas from noaa table~~
- ~~Define a transmission protocol~~
- ~~implement file system based transmission~~
- ~~Transmit noaa~~
- ~~Update noaa attributed to reflect transmission~~
- ~~Implement a email based transmission~~
- ~~Define a Leiningen task to kick off the identification pass.~~
- ~~Define a Leiningen task to kick off the generation pass.~~



### 3.5: Weaponize What We Have So Far
- ~~Store data used to generate message in noaa table.~~
- Connect to Clarity for realz.
- ~~Align the lead db schema with reality.~~
- ~~Handle db exceptions gracefully~~
- ~~Write some basic sanity tests that involve setting up/tearing down info.~~
- Get the actual text used for the templates
- Include originating bank as a qualifier for template choice
- ~~Identify what we actually need from Clarity and pluck it from the report as part of attach-clarity-report in generation.~~
- ~~Move Migratus operations into a setting that honors environment configs.~~
- Read business-rules-denied leads from SF and include them in the identification pass.


### 4:  Build a NOAA production environment.
- create a production node
- create a production db
- get lead data etl'ed into the production db
- Create secrets for production node
- connect to production db
- Create noaa table in production
- define a production test
- review production test


### 5: Build a NOAA test environment
- Create a test node
- Create secrets for test environment
- Connect to test database
- Create noaa lead table in test db
- Write a job to deploy code to the test node
- integrate with sumo
- Integrate with sentry
- define and write integration tests


### BACKLOG
- Add a rolling window to the identify-leads-needing-noaas query so we don't do needless retrievals.
- Store delivery status as part of NOAA during delivery pass
