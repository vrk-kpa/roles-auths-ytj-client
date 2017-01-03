## Getting Started
Maven commands:
* mvn install
* mvn -Dserver.port=8907 spring-boot:run

## Start standalone jar
java -Dserver.port=8007 -jar target/roles-auths-ytj-client.jar

## Adding new wsdl 
When new version of AuthorizationQueryService.wsdl or CompanyQueryService.wsdl is added to the project, xroad types has to be removed manually from them and replaced with

 <xs:import id="xrd" namespace="http://x-road.eu/xsd/xroad.xsd" schemaLocation="xroad.xsd" /> 
 
## License headers

The license headers in source code files have been generated using the maven-license-plugin and enforced during project build.

If the build fails due to missing license headers they can be updated easily using `mvn license:format`.

